/**
* gcalendar-notification-client-1.0.js
* 
* @author Bertrand Martel
* @version 1.0
* 
* @description
*   websocket API processing to :
*   _ request oauth registration to obtain verification url and user code to authorize use of google API through google account
*   _ request access token to be used for using google API (calendar + profile)
*   _ revoke access token at will
*   _ request user profile 
*   _ request calendar events on a specific date time range (and with text filter)
*   _ create calendar event (summary + date start + date end)
*   _ delete calendar event
*   _ subscribe/unsubscribe to one calendar event : this will trigger notification to appear in browser when the event is about to start and when the event will actually start 
*/

// datatable object
var table;

//websocket object
var ws;

/**
* Hide all notification message
* see https://dhirajkumarsingh.wordpress.com/2012/05/06/cool-notification-messages-with-css3-jquery/
*/
function hideAllMessages(){
   var messagesHeight;
   messagesHeight = $('.info').outerHeight(); // fill array
   $('.info').css('top', -messagesHeight); //move element outside viewport
}

/**
* subscribe or unsubscribe to calendar event
* @state
*   subscribing state
* @eventId
*   calendar event id
*/
function updateSubscription(state,eventId)
{
  if (ws.readyState==1)
  {
    if (typeof table!=="undefined")
    {
      if (state)
      {
        ws.send('{"action":"subscribeEvent","eventId":"' +  eventId + '","timeAboutToStart":60}');
      }
      else
      {
        ws.send('{"action":"unsubscribeEvent","eventId":"' +  eventId + '"}');
      }
    }
    else
    {
      console.log("datatable has not been loaded yet!");
    }
  }
  else
  {
    document.getElementById("infoMessage3").innerHTML="subscription failure ...";
  }
}

/**
* open calendar creation dialog
*/
function createCalendarEvent()
{
  $( "#createEvent" ).dialog("open");
}

/**
* request calendar event creation on submit
*/
function sendCalendarEventCreation()
{
  $( "#createEvent" ).dialog("close");

  var summary=document.getElementById("summary").value;
  var createEventDateStart = document.getElementById("createEventDateStart").value;
  var createEventDateEnd = document.getElementById("createEventDateEnd").value;

  if (createEventDateStart=="" || createEventDateEnd=="")
  {
    alert("Error date cant be empty");
    return;
  }
  if (createEventDateStart.length!=16 || createEventDateEnd.length!=16)
  {
    alert("Error invalid date format");
    return;  
  }
  var dateStart = new Date(createEventDateStart);
  var dateEnd = new Date(createEventDateEnd);

  if (dateStart>=dateEnd)
  {
    alert("Error date range is invalid. Check that date start < date end");
    return;
  }

  if (ws.readyState==1)
  {
      ws.send('{"action":"createCalendarEvent","dateBegin":"' +  dateFormat(dateStart) + '","dateEnd":"' + dateFormat(dateEnd)+ '","summary":"' + summary + '"}');
  }
  else
  {
    document.getElementById("infoMessage3").innerHTML="create event failure ...";
  }
}

/**
* delete an event from google calendar (when row is selected)
*/
function deleteCalendarEvent()
{
  if (ws.readyState==1)
  {
    if (typeof table!=="undefined")
    {
      var eventId=table.cell('.selected', 0).data();

      table.row('.selected').remove().draw( false );

      ws.send('{"action":"deleteCalendarEvent","eventId":"' +  eventId + '"}');
    }
    else
    {
      console.log("datatable has not been loaded yet!");
    }
  }
  else
  {
    document.getElementById("infoMessage3").innerHTML="event deletion failure ...";
  }
}

/**
* Retrieve all calendar events on a specific date time range
*/
function getCalendarEvents()
{
  if (ws.readyState==1)
  {
    document.getElementById("infoMessage3").innerHTML="calendar events requesting...";

    var dateStart = new Date(document.getElementById("dateStart").value);

    var dateEnd = new Date(document.getElementById("dateEnd").value);

    var searchText = document.getElementById("filterText").value;

    ws.send('{"action":"getCalendarEvents","dateBegin":"' + dateFormat(dateStart) + '","dateEnd":"' + dateFormat(dateEnd) + '","searchText":"' + searchText + '"}');

  }
  else
  {
    document.getElementById("infoMessage3").innerHTML="retrieve events failure ...";
  }
}

/**
* retrieve user profile
*/
function getUserProfile()
{
  if (ws.readyState==1)
  {
    document.getElementById("infoMessage3").innerHTML="retrieving user profile ...";
    ws.send('{"action":"userProfile"}');
  }
  else
  {
    document.getElementById("infoMessage3").innerHTML="retrieving user profile failed ...";
  }
}

/**
* revoke current access token
*/
function revokeToken()
{
  if (ws.readyState==1)
  {
    document.getElementById("infoMessage2").innerHTML="revoking token ...";
    ws.send('{"action":"revokeToken"}');
  }
  else
  {
    document.getElementById("infoMessage2").innerHTML="revoking token failed ...";
  }
}

/**
* start google oauth registration
*/
function startRegistration()
{
  if (ws.readyState==1)
  {
    document.getElementById("infoMessage").innerHTML="starting registration ...";
    ws.send('{"action":"registration"}');
  }
  else
  {
    document.getElementById("infoMessage").innerHTML="starting registration failed ...";
  }
}

/**
* request a token once user is authenticated => an error response will be answered back to websocket if user did not authenticate with specified user code
*/
function requestToken()
{
  if (ws.readyState==1)
  {
    document.getElementById("infoMessage2").innerHTML="requesting token ...";
    ws.send('{"action":"requestToken"}');
  }
  else
  {
    document.getElementById("infoMessage").innerHTML="requesting token failed ...";
  }
}

/**
* format a date from Date object to YYYY/mm/dd hh:mm:ss format
*/
function dateFormat2(date)
{
  var dd = date.getDate();
  var mm = date.getMonth()+1;
  var yyyy = date.getFullYear();

  var hour= date.getHours();
  var minutes=date.getMinutes();
  var seconds=date.getSeconds();

  if (hour<10){
    hour='0'+hour;
  }
  if (minutes<10){
    minutes='0'+minutes;
  }
  if (seconds<10){
    seconds='0'+seconds;
  }

  if(dd<10){
    dd='0'+dd
  } 
  if(mm<10){
    mm='0'+mm
  }

  return yyyy + "/" + mm + "/" + dd + " " + hour + ":" + minutes; 
}

/**
* format a date from Date object to RFC 3339 timestamp specification (including timezone offset)
*/
function dateFormat(date)
{
  var dd = date.getDate();
  var mm = date.getMonth()+1;
  var yyyy = date.getFullYear();

  var hour= date.getHours();
  var minutes=date.getMinutes();
  var seconds=date.getSeconds();

  if (hour<10){
    hour='0'+hour;
  }
  if (minutes<10){
    minutes='0'+minutes;
  }
  if (seconds<10){
    seconds='0'+seconds;
  }

  if(dd<10){
    dd='0'+dd
  } 
  if(mm<10){
    mm='0'+mm
  } 

  var n = date.getTimezoneOffset()/60;
  var offset="";
  
  if (n>0){
    offset="-";
  }
  else{
    offset="+"
    n=(-1)*n;
  }

  if (n<9){
    offset+="0"+n;
  }
  else{
    offset+=n;
  }

  var result = yyyy+'-'+mm+'-'+dd +"T" + hour +":" + minutes + ":" + seconds +offset + ":00";

  return result;
}

/**
* initialize date field values
*/
function initDate()
{
  var today = new Date();
  today.setHours(23);
  today.setMinutes(59);
  today.setSeconds(00);

  var lastWeek = new Date();
  lastWeek.setDate(lastWeek.getDate()-7);
  lastWeek.setHours(00);
  lastWeek.setMinutes(00);
  lastWeek.setSeconds(00);

  document.getElementById("dateStart").value=dateFormat2(lastWeek);
  document.getElementById("dateEnd").value=dateFormat2(today);
}

/**
* initialize notification
*/
function initNotification()
{
  $(document).ready(function(){
       // Initially, hide them all
       hideAllMessages();
       
       // When message is clicked, hide it
       $('.message').click(function(){        
            $(this).animate({top: -$(this).outerHeight()}, 500);
        });    
  });
}

// start a websocket client connecting to our local server
// Manage all API
function launchWebsocketClient()
{
  initNotification();

  initDate();

  if ("WebSocket" in window)
  {
     //open websocket on port 8443
     ws = new WebSocket("ws://127.0.0.1:4343");

     ws.onopen = function()
     {
        document.getElementsByTagName("html")[0].style.visibility = "visible";
        console.log("Connection to server with url : " + ws.URL);

        //send Hello to server
        ws.send('{"action":"registrationState"}');
        ws.send('{"action":"tokenState"}');
        getCalendarEvents();
     };

     ws.onmessage = function (evt) 
     { 

        console.log("Message received from websocket : " + evt.data);

        
        var data = JSON.parse(evt.data); 

        if (data.hasOwnProperty("deviceCode") && 
              data.hasOwnProperty("expiringBefore") && 
                data.hasOwnProperty("interval") &&
                  data.hasOwnProperty("userCode") &&
                    data.hasOwnProperty("verificationUrl"))
        {
          document.getElementById("infoMessage").innerHTML="redirection to verification url ...";

          document.getElementById("two").style.display="block";

          document.getElementById("verificationUrl").innerHTML='<a href=' + data["verificationUrl"] + ' target="_blank">' + data["verificationUrl"] + '</a>';

          document.getElementById("userCode").innerHTML=data["userCode"];
          
          document.getElementById("requestTokenBtn").style.display="block";

          document.getElementById("requestTokenDiv").style.display="block";
        }
        else if (data.hasOwnProperty("accessToken") && 
              data.hasOwnProperty("tokenType") && 
                data.hasOwnProperty("expireIn"))
        {

          document.getElementById("two2").style.display="block";
          document.getElementById("accessToken").innerHTML=data["accessToken"];
          document.getElementById("expireIn").innerHTML=data["expireIn"];
          document.getElementById("calendar_div").style.display="block";
          document.getElementById("revokeTokenBtn").style.display="block";
          getCalendarEvents();
        }
        else if (data.hasOwnProperty("revokeToken"))
        {
            document.getElementById("infoMessage2").innerHTML="revoke token success !";

            document.getElementById("two").style.display="none";
            document.getElementById("two2").style.display="none";
            document.getElementById("calendar_div").style.display="none";
            document.getElementById("requestTokenBtn").style.display="none";
            document.getElementById("revokeTokenBtn").style.display="none";

            document.getElementById("accessToken").innerHTML="";
            document.getElementById("expireIn").innerHTML="";
        }
        else if (data.hasOwnProperty("gender") && 
                    data.hasOwnProperty("displayName") && 
                      data.hasOwnProperty("familyName") && 
                        data.hasOwnProperty("givenName") && 
                          data.hasOwnProperty("language"))
        {
          document.getElementById("infoMessage3").innerHTML="user profile retrieved !";

          document.getElementById("gender").innerHTML=data["gender"];
          document.getElementById("displayName").innerHTML=data["displayName"];
          document.getElementById("familyName").innerHTML=data["familyName"];
          document.getElementById("givenName").innerHTML=data["givenName"];
          document.getElementById("language").innerHTML=data["language"];

          $( "#userProfile" ).dialog("open");
        }
        else if (data.hasOwnProperty("eventList"))
        {
          document.getElementById("infoMessage3").innerHTML="event list recived";
          var dataSet =[];

          if (data.hasOwnProperty("eventList"))
          {
            var array =  data["eventList"];

            for (i in array)
            {
              var item = array[i];

              if (item["dateStart"]=="" && item["dateTimeStart"]!="")
              {
                item["dateStart"]=item["dateTimeStart"];
              }
              if (item["dateEnd"]=="" && item["dateTimeEnd"]!="")
              {
                item["dateEnd"]=item["dateTimeEnd"];
              }

              var itemDataSet = [ item["eventId"],item["dateStart"],item["dateEnd"],item["summary"],item["dateCreated"],item["creatorEmail"],item["creatorDisplayName"],item["subscribed"]];

              dataSet.push(itemDataSet);
            }
            
            $(document).ready(function() {
                
                if (typeof table!=="undefined")
                {
                  table.destroy();
                }

                table  = $('#calendarTable').DataTable( {
                    "data": dataSet,
                    "columns": [
                        { "title": "event id" },
                        { "title": "date start" },
                        { "title": "date end" },
                        { "title": "summary" },
                        { "title": "date creation" },
                        { "title": "creator email" },
                        { "title": "creator name" },
                        {
                            "title":   "subscribed",
                            render: function ( data, type, row ) {
                                if (data) {
                                    return '<input type="checkbox" onClick="updateSubscription(this.checked,\'' + row[0] + '\');" checked>';
                                }
                                else
                                {
                                    return '<input type="checkbox" onClick="updateSubscription(this.checked,\'' + row[0] + '\');" >';
                                }
                                return data;
                            },
                            className: "dt-body-center"
                        }
                    ],
                    "aoColumnDefs": [
                      { "bSearchable": false, "bVisible": false, "aTargets": [ 0 ] },
                    ]  
                } );
                
                $('#calendarTable tbody').off( 'click' );

                $('#calendarTable tbody').on( 'click', 'tr', function () {
                    if ( $(this).hasClass('selected') ) {
                        $(this).removeClass('selected');
                    }
                    else {
                        table.$('tr.selected').removeClass('selected');
                        $(this).addClass('selected');
                    }
                } );

            } );
            
          }
          
        }
        else if (data.hasOwnProperty("deleteEvent") && data.hasOwnProperty("eventId"))
        {
          console.log("An event has been removed");

        }
        else if (data.hasOwnProperty("createEvent"))
        {
          console.log("event created. Refreshing list");
          getCalendarEvents();
        }
        else if (data.hasOwnProperty("subscribedEvent") && data.hasOwnProperty("eventType") && data.hasOwnProperty("summary"))
        {

          console.log("subscribedEvent for event " + data["subscribedEvent"] + " of type => " + data["eventType"]);

          if (data["eventType"]=="aboutToStart"){
            document.getElementById("eventSummary").innerHTML=data["summary"] + " is about to start !";
            hideAllMessages();
                  $('.'+"info").animate({top:"0"}, 500);
          }
          else if (data["eventType"]=="started")
          {
            document.getElementById("eventSummary").innerHTML=data["summary"] + " has started !";
            hideAllMessages();
                  $('.'+"info").animate({top:"0"}, 500);
          }
        }
        else if (data.hasOwnProperty("error") && data.hasOwnProperty("error_description"))
        {
          if (data["error_description"]=="")
            document.getElementById("infoMessage2").innerHTML="error occured ...";
          else
            document.getElementById("infoMessage2").innerHTML="error occured : " + data["error_description"];
        }
        else
        {
          //document.getElementById("infoMessage").innerHTML="something is amiss";
        }
     };

     ws.onclose = function()
     { 
        console.log("Connection has been closed");
     };

     ws.onerror = function()
     {
        document.getElementsByTagName("html")[0].style.visibility = "visible";
        console.log("Websocket connection error");
     }
  }
  else
  {
     alert("WebSocket is NOT supported by your Browser!");
  }
}

/**
* delete a row in datatable
*/
function deletedatarow(eventId){
  $( "tr:contains('" + eventId + "')").each(function() {
    table.fnDeleteRow(this);
  });
};

//close websocket if exists
function closeWebsocketClient()
{
  if (typeof(ws)!="undefined")
     ws.close();
}
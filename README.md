# Google Calendar event notification server #

<i>Last update on 09/08/2015</i>

Embedded google calendar event notification server through Google Calendar API (Oauth2.0)

This project is an alternative for "end device" to Google Calendar push notification (https://developers.google.com/google-apps/calendar/v3/push?hl=en) where you need to have a domain receiving your notifications which force your application to have a back end realizing the job for your client.

2 parts are featured :
* gcalendar-notification-lib which is library you would used to manage Google Calendar API and process events notifications
* gcalendar-notification-webserver which is a server implementation and message process management which expose its API for a web client

If you have your own server implementation, you can integrate gcalendar-notification-lib separately without the server part

<h3>What does it do ?</h3>

* receive notifications of all events you have subscribed through gcalendar-notification-lib APIs that are pushed to a web client
* subscribe/unsubscribe to a precise event
* create an event (with date start, date end and summary)
* delete an event
* retrieve list of Google calendar events for a specific date time range and optionnal text filter
* retrieve basic user profile information
* request an access token that will be used to request Google API for the scope Profile and Calendar
* revoke an access token (if you want to)
* register through Oauth2.0

[ ! ] Event creation is very basic for now (date start / date begin and summary) but is a good medium to test notifications

<h3>Run the testing server app</h3>

``git clone git@github.com:akinaru/gcalendar-notification-server.git``

``cd gcalendar-notification-server/gcalendar-notification-webserver/release``

``java -jar gcalendar-notification-client-webserver-1.0.jar webPath=<your_absolute_path>/gcalendar-notification-server/web clientId=<your_client_id> clientSecret=<your_client_secret>``

_ replace <your_absolute_path> with your path prior to gcalendar-notification-webserver 
_ replace <your_client_id> and <your_client_secret> with the ones you got from google developper console

* go to you browser : <a>http://localhost:4242/gcalendar</a>

* now you can click on "start registration" button to request registration. You will see a verification url appear and a user code.

<i>If you dont see these two features appear maybe you have a problem with your client id / client secret</i>

* go to specified URL and authorize access from Google API.

* once it is done, you can click on "request token" button, and shortly a token will appear

* From this moment you will have access to "user profile", "calendar events", "create event" and "delete event" buttons

* You can create a fake event of your own with "create event" button putting the date time range of your choice

* You can subscribe to events to come (even events already existing) and you will see a notification coming to your window when event will be about to start (60 seconds befor event) or when the event has actually started

<h3>Oauth2.0 for device registration</h3>

```
String yourOwnClientId="812741506391-h38jh0j4fv0ce1krdkiq0hfvt6n5amrf.apps.googleusercontent.com";
String yourClientSecret="1912308409123890";
```

Now you can  instantiate ``AuthenticationManager`` class :

```
CalendarNotifManager calendarNotifManager = new CalendarNotifManager(yourOwnClientId, yourClientSecret);
```

Then you can request authentication :

```
calendarNotifManager.requestDeviceAuth(new IOauthDeviceResponseListener() {

	@Override
	public void onResponseReceived(OauthForDeviceResponse response) {
		
		/* callback called when response is received from server*/

	}
});
```

Description of ``OauthForDeviceResponse`` :
* ``getVerificationUrl()`` : url from which user will log and therefore authorize your server to request token
* ``getUserCode()`` : code that user will reproduce exactly in the latter verification url
* ``getDeviceCode()`` : code identifying your server which is viewed as an end device requesting google api
* ``getExpiringBefore()`` : life time of your usercode in seconds
* ``getInterval()`` : time interval between a next attempt in seconds

The verification url is displayed to the user via a web client.
The user will reproduce the user code from the same web client and authorize the server to request token.
From this point you will be able to request token.

The whole authentication process is described in https://developers.google.com/identity/protocols/OAuth2ForDevices

<h3>Request for access token</h3>

```
calendarNotifManager.requestToken(new IRequestTokenListener() {

	@Override
	public void onRequestTokenReceived(OauthToken token) {
		//access token is received
	}

	@Override
	public void onRequestTokenError(String description) {
		//an error occured requesting access token
	}
});
```

Once you have received a token you can access Google calendar API and profile.

<h3>Request user profile</h3>

```
calendarNotifManager.getUserProfileManager().getUserProfile(new IUserProfileListener() {

	@Override
	public void onSuccess(UserProfile userProfile) {

		// user profile received succcessfully :

		//userProfile.getGender()
		//userProfile.getDisplayName()
		//userProfile.getFamilyName()
		//userProfile.getGivenName()
		//userProfile.getLanguage()
	}

	@Override
	public void onError(String description) {
		//an error occured requesting user profile
	}
});
```

<h3>Request calendar events</h3>

You can request event with date time range from ``dateBegin`` to ``dateEnd`` with these two values in String timestamp format according RFC 3339 (ex: 2015-08-06T23:30:20+02:00)
You can optionnaly add a text filter

```
calendarNotifManager.getCalendarManager().getEventList(dateBegin, dateEnd, searchText, new IEventListListener() {

	@Override
	public void onEventListReceived(List<CalendarEvents> calendarEventList) {
		//list of calendar events retrieved
	}

	@Override
	public void onError(String description) {
		//an error occured requesting calendar events
	}
});
```

Here is description of one event in List<CalendarEvents> retrieved :
* ``String getEventId()`` : event identifier
* ``String getStatus()``: event status
* ``String getDateCreated()`` : creation date of the event
* ``String getDateUpdated()`` : date of event udpate
* ``String getSummary()`` : event summary
* ``String getCreatorEmail()`` : event creator email
* ``String getCreatorDisplayName()`` : event creator display name
* ``boolean isCreatorSelf()`` : true if the event was created by calendar's owner
* ``String getOrganizerEmail()`` : event organizer email
* ``String getOrganizerDisplayName()`` : event organizer display name
* ``boolean isOrganizerSelf()`` : true if event organizer is calendar's owner
* ``String getDateStart()`` : event date start 
* ``String getDateEnd()`` : event date end 
* ``String getDateTimeStart()`` : event date and time start
* ``String getDateTimeEnd()`` : event date and time end
* ``boolean isSubscribed()`` : define if this event is subscribed or not (you will receive notification if true)

<h3>Create event</h3>

You can create an event with begin and end date time of this event in String timestamp format according to RFC 3339 (ex: 2015-08-06T23:30:20+02:00) and precise a summary for this event.

```
calendarNotifManager.getCalendarManager().createEvent(dateBegin, dateEnd, summary, new ICreateEventListener() {

	@Override
	public void onCreateSuccess(String id) {
		// event has been successfully created. The event id is returned on creation success
	}

	@Override
	public void onError(String description) {
		//an error occured during event creation
	}

});
```

<h3>Delete event</h3>

You can delete an evetn by ``eventId`` 

```
calendarNotifManager.getCalendarManager().deleteEvent(eventId, new IDeleteEventListener() {

	@Override
	public void onSuccess() {
		//event has been deleted
	}

	@Override
	public void onError(String description) {
		//an error occured during deletion process
	}
});
```

<h3>Event notification : subscription</h3>

You can be notified for a specified event referenced by its ``eventId``. You will be notified when the event start and you can parameter the time in seconds before it actually starts when you want to be notified.

If you want to be notified one minute before the event start replace ``timeAboutToStart`` by 60

```
calendarNotifManager.getNotificationManager().subscribeEvent(eventId, timeAboutToStart, new IEventListener() {

	@Override
	public void onEventStart(String eventId, String summary) {
		//called when the event start
	}

	@Override
	public void onEventAboutToStart(String eventId, String summary) {
		//called when the event is about to start (if you put a value >0 for timeAboutToStart parameter)
	}
});
```

<h3>Unsubscribtion</h3>

```
calendarNotifManager.getNotificationManager().unsubscribeEvent(eventId);
```

<h3>Get an Oauth2.0 token from Google developper console</h3>

For your application to be abled to request token for Oauth2.0 google API, you have to get one Oauth2.0 token from https://console.developers.google.com

* First create a project
* in "credentials" tab "create a new client ID" choose "installed application",quote "other" and "create client ID"
* in "consent screen" tab choose tour email address and a product name (it should apparently match your project id name but I may be wrong here)

Now in "crendentials" tab you should have an Oauth2.0 token client ID that looks like :<br/>
``812741506391-h38jh0j4fv0ce1krdkiq0hfvt6n5amrf.apps.googleusercontent.com``

* In "API" tab select "Google + API" and enable it
* In "API" tab select "Calendar API" and enable it

<b>External JAVA Library</b>

* json-simple  : http://code.google.com/p/json-simple/

* clientsocket : https://github.com/akinaru/socket-multiplatform/tree/master/client/socket-client/java

* http-endec   : https://github.com/akinaru/http-endec-java

* serversocket : https://github.com/akinaru/socket-multiplatform/tree/master/server/server-socket/blocking/java

* websocket-java : https://github.com/akinaru/websocket-java

<b>External UI features</b>

* datetimepicker : https://github.com/xdan/datetimepicker

* notification message using css3 : https://dhirajkumarsingh.wordpress.com/2012/05/06/cool-notification-messages-with-css3-jquery/

<b>TODO</b>
_ stock raw configuration of events subscribed
_ improve "create event" input arguments (attendees / place ...)
_ configurable polling of "getEventList" API in case of concurrent modifications
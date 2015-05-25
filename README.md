# Sending SMS with Google via Google Calendar and Google Oauth 2.0 API #

<i>UNDER CONSTRUCTION</i>

<i>Last update on 25/05/2015</i>

A small server<->client enabling you to send a SMS to your phone bound to your Google account, through Google Calendar API (Oauth2.0)

Basically, once authorization to your Google Calendar profile is granted; when you want to send a SMS : 
* smartphone notifications will be switched ON in your Google Calendar parameters
* a fake event will be created in your Google Calendar 5 minutes after the current date
* you will receive the SMS notification (with your custom body)
* the fake event will be removed from Google Calendar once we are done

<b>Registration to Google Oauth2.0 API</b>

* 1) server request an usercode and a verification url for the user to be able to access his/her Google login page.
* 2) these verification url and usercode are sent to user interface.
* 3) Web client open the google device login page in which the user will have to copy/paste the usercode (shown on the web page)
* 4) Eventually, by clicking OK user grants the server access to Google Oauth2.0 API with specified profiles that were requested

This process is described in https://developers.google.com/identity/protocols/OAuth2ForDevices

<b>Get an Oauth2.0 token from Google developper console</b>

For your application to be abled to request token for Oauth2.0 google API, you have to get one Oauth2.0 token from https://console.developers.google.com

* First create a project
* in "credentials" tab "create a new client ID" choose "installed application",quote "other" and "create client ID"
* in "consent screen" tab choose tour email address and a product name (it should apparently match your project id name but I may be wrong here)

Now in "crendentials" tab you should have an Oauth2.0 token client ID that looks like : 
``812741506391-h38jh0j4fv0ce1krdkiq0hfvt6n5amrf.apps.googleusercontent.com``

It will serve you as input for getting one usercode and url verification later.

<b>Java server (UNDER CONSTRUCTION)</b>


<b>Angular JS web client (UNDER CONSTRUCTION)</b>


<b>Library used</b>

* json-simple  : http://code.google.com/p/json-simple/

* clientsocket : https://github.com/akinaru/socket-multiplatform/tree/master/client/socket-client/java

* http-endec   : https://github.com/akinaru/http-endec-java

* serversocket : https://github.com/akinaru/socket-multiplatform/tree/master/server/server-socket/blocking/java
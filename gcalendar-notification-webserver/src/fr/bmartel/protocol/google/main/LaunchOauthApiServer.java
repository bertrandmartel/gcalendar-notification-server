/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Bertrand Martel
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.protocol.google.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import fr.bmartel.protocol.google.api.calendar.CalendarEvents;
import fr.bmartel.protocol.google.api.calendar.CalendarUtils;
import fr.bmartel.protocol.google.api.calendar.ICreateEventListener;
import fr.bmartel.protocol.google.api.calendar.IDeleteEventListener;
import fr.bmartel.protocol.google.api.calendar.IEventListListener;
import fr.bmartel.protocol.google.api.calendar.IEventListener;
import fr.bmartel.protocol.google.api.profile.IUserProfileListener;
import fr.bmartel.protocol.google.api.profile.UserProfile;
import fr.bmartel.protocol.google.constants.JsonConstants;
import fr.bmartel.protocol.google.oauth.device.CalendarNotifManager;
import fr.bmartel.protocol.google.oauth.device.IOauthDeviceResponseListener;
import fr.bmartel.protocol.google.oauth.device.IRequestTokenListener;
import fr.bmartel.protocol.google.oauth.device.IRevokeTokenListener;
import fr.bmartel.protocol.google.oauth.device.OauthForDeviceResponse;
import fr.bmartel.protocol.google.oauth.device.OauthToken;
import fr.bmartel.protocol.http.HttpResponseFrame;
import fr.bmartel.protocol.http.HttpVersion;
import fr.bmartel.protocol.http.constants.StatusCodeList;
import fr.bmartel.protocol.http.inter.IHttpFrame;
import fr.bmartel.protocol.http.listeners.IHttpServerEventListener;
import fr.bmartel.protocol.http.server.HttpServer;
import fr.bmartel.protocol.http.server.IHttpStream;
import fr.bmartel.protocol.http.states.HttpStates;
import fr.bmartel.protocol.websocket.listeners.IClientEventListener;
import fr.bmartel.protocol.websocket.server.IWebsocketClient;
import fr.bmartel.protocol.websocket.server.WebsocketServer;
import fr.bmartel.utils.FileUtils;

/**
 * Interact with gcalendar notification library and expose to web client to :
 * _registrate via oauth on web client _request access token _revoke access
 * token _request user profile _retrieve google calendar events on a specific
 * date time range _create event from web client _delete evetn from web client
 * _subscribe/unsubscribe to specific event at will _send notification when
 * subscribed event is about to start or has started.
 *
 * @author Bertrand Martel
 */
public class LaunchOauthApiServer {

	/** Web server port. */
	private final static int SERVER_PORT = 4242;

	/** Websocket server port. */
	private final static int WEBSOCKET_SERVER_PORT = 4343;

	/** The websocket server. */
	private static WebsocketServer websocketServer = null;

	/** The web path. */
	private static String webPath = "";

	/** The client id. */
	private static String clientId = "";
	
	/** The client secret. */
	private static String clientSecret = "";

	/** Calendar api manager object. */
	private static CalendarNotifManager calendarNotifManager = null;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		webPath = "";
		clientId = "";
		clientSecret = "";

		if (args.length == 3) {

			for (int i = 0; i < 3; i++) {
				if (args[i].toLowerCase().startsWith("webpath="))
					webPath = args[i].substring(args[i].indexOf("webpath=") + "webpath=".length() + 1, args[i].length());
				if (args[i].toLowerCase().startsWith("clientid="))
					clientId = args[i].substring(args[i].indexOf("clientid=") + "clientid=".length() + 1, args[i].length());
				if (args[i].toLowerCase().startsWith("clientsecret="))
					clientSecret = args[i].substring(args[i].indexOf("clientsecret=") + "clientsecret=".length() + 1, args[i].length());
			}

			if (webPath.equals("")) {
				printHelp("Error web path is missing");
				return;
			} else if (clientId.equals("")) {
				printHelp("Error client Id is missing");
				return;
			} else if (clientSecret.equals("")) {
				printHelp("Error client secret is missing");
				return;
			}
		} else {
			printHelp("");
			return;
		}
		// start http server
		HttpServer server = new HttpServer(SERVER_PORT);

		websocketServer = new WebsocketServer(WEBSOCKET_SERVER_PORT);

		websocketServer.addServerEventListener(new IClientEventListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onMessageReceivedFromClient(final IWebsocketClient client, String message) {

				JSONObject obj = (JSONObject) JSONValue.parse(message);

				if (obj != null && obj.containsKey(JsonConstants.API_ACTION)) {

					System.out.println("[API] > " + obj.toJSONString());

					String action = obj.get(JsonConstants.API_ACTION).toString();

					if (action != null) {
						if (action.equals(JsonConstants.API_REGISTRATION_STATE)) {

							JSONObject registrationResponse = new JSONObject();

							if (calendarNotifManager != null && calendarNotifManager.getOauthRegistration() != null) {

								registrationResponse.put(JsonConstants.GOOGLE_OAUTH_DEVICE_CODE, calendarNotifManager.getOauthRegistration().getDeviceCode());
								registrationResponse.put(JsonConstants.GOOGLE_OAUTH_EXPIRING_BEFORE, calendarNotifManager.getOauthRegistration()
										.getExpiringBefore());
								registrationResponse.put(JsonConstants.GOOGLE_OAUTH_INTERVAL, calendarNotifManager.getOauthRegistration().getInterval());
								registrationResponse.put(JsonConstants.GOOGLE_OAUTH_USERCODE, calendarNotifManager.getOauthRegistration().getUserCode());
								registrationResponse.put(JsonConstants.GOOGLE_OAUTH_VERIFICATION_URL, calendarNotifManager.getOauthRegistration()
										.getVerificationUrl());

							}

							System.out.println("[API] < " + registrationResponse.toJSONString());
							client.sendMessage(registrationResponse.toJSONString());

						} else if (action.equals(JsonConstants.API_TOKEN_STATE)) {

							JSONObject requestTokenResponse = new JSONObject();

							if (calendarNotifManager != null && calendarNotifManager.getCurrentToken() != null) {

								requestTokenResponse.put(JsonConstants.GOOGLE_OAUTH_ACCESS_TOKEN, calendarNotifManager.getCurrentToken().getAccessToken());
								requestTokenResponse.put(JsonConstants.GOOGLE_OAUTH_TOKEN_TYPE, calendarNotifManager.getCurrentToken().getTokenType());
								requestTokenResponse.put(JsonConstants.GOOGLE_OAUTH_EXPIRE_IN, calendarNotifManager.getCurrentToken().getExpiresIn());

							}
							System.out.println("[API] < " + requestTokenResponse.toJSONString());
							client.sendMessage(requestTokenResponse.toJSONString());

						} else if (action.equals(JsonConstants.API_REGISTRATION)) {

							calendarNotifManager = new CalendarNotifManager(clientId, clientSecret);

							calendarNotifManager.requestDeviceAuth(new IOauthDeviceResponseListener() {

								@Override
								public void onResponseReceived(OauthForDeviceResponse response) {
									if (response != null) {
										JSONObject registrationResponse = new JSONObject();
										registrationResponse.put(JsonConstants.GOOGLE_OAUTH_DEVICE_CODE, response.getDeviceCode());
										registrationResponse.put(JsonConstants.GOOGLE_OAUTH_EXPIRING_BEFORE, response.getExpiringBefore());
										registrationResponse.put(JsonConstants.GOOGLE_OAUTH_INTERVAL, response.getInterval());
										registrationResponse.put(JsonConstants.GOOGLE_OAUTH_USERCODE, response.getUserCode());
										registrationResponse.put(JsonConstants.GOOGLE_OAUTH_VERIFICATION_URL, response.getVerificationUrl());

										System.out.println("[API] < " + registrationResponse.toJSONString());

										client.sendMessage(registrationResponse.toJSONString());
									}
								}
							});
						} else if (action.equals(JsonConstants.API_REQUEST_TOKEN)) {

							if (calendarNotifManager != null) {
								calendarNotifManager.requestToken(new IRequestTokenListener() {

									@Override
									public void onRequestTokenReceived(OauthToken token) {
										if (token != null) {
											JSONObject requestTokenResponse = new JSONObject();
											requestTokenResponse.put(JsonConstants.GOOGLE_OAUTH_ACCESS_TOKEN, token.getAccessToken());
											requestTokenResponse.put(JsonConstants.GOOGLE_OAUTH_TOKEN_TYPE, token.getTokenType());
											requestTokenResponse.put(JsonConstants.GOOGLE_OAUTH_EXPIRE_IN, token.getExpiresIn());

											System.out.println("[API] < " + requestTokenResponse.toJSONString());

											client.sendMessage(requestTokenResponse.toJSONString());
										}
									}

									@Override
									public void onRequestTokenError(String description) {
										String response = "{\"error\":\"request token error\",\"error_description\":\"" + description + "\"}";

										System.out.println("[API] < " + response);

										client.sendMessage(response);
									}
								});
							}
						} else if (action.equals(JsonConstants.API_REVOKE_TOKEN)) {

							if (calendarNotifManager != null) {
								calendarNotifManager.revokeToken(new IRevokeTokenListener() {

									@Override
									public void onSuccess() {

										System.out.println("[API] < " + "{\"revokeToken\":\"success\"}");

										client.sendMessage("{\"revokeToken\":\"success\"}");
									}

									@Override
									public void onError(String description) {

										String response = "{\"error\":\"request token error\",\"error_description\":\"" + description + "\"}";

										System.out.println("[API] < " + response);

										client.sendMessage(response);
									}
								});
							}
						} else if (action.equals(JsonConstants.API_USER_PROFILE)) {
							if (calendarNotifManager != null) {
								calendarNotifManager.getUserProfileManager().getUserProfile(new IUserProfileListener() {

									@Override
									public void onSuccess(UserProfile userProfile) {
										if (userProfile != null) {
											JSONObject userProfileResponse = new JSONObject();
											userProfileResponse.put(JsonConstants.GOOGLE_API_PROFILE_GENDER, userProfile.getGender());
											userProfileResponse.put(JsonConstants.GOOGLE_API_PROFILE_DISPLAY_NAME, userProfile.getDisplayName());
											userProfileResponse.put(JsonConstants.GOOGLE_API_PROFILE_FAMILY_NAME, userProfile.getFamilyName());
											userProfileResponse.put(JsonConstants.GOOGLE_API_PROFILE_GIVEN_NAME, userProfile.getGivenName());
											userProfileResponse.put(JsonConstants.GOOGLE_API_PROFILE_LANGUAGE, userProfile.getLanguage());

											System.out.println("[API] < " + userProfileResponse.toJSONString());

											client.sendMessage(userProfileResponse.toJSONString());
										}
									}

									@Override
									public void onError(String description) {

										String response = "{\"error\":\"request token error\",\"error_description\":\"" + description + "\"}";

										System.out.println("[API] < " + response);

										client.sendMessage(response);
									}
								});
							}
						} else if (action.equals(JsonConstants.API_CREATE_EVENT) && obj.containsKey(JsonConstants.API_DATE_BEGIN)
								&& obj.containsKey(JsonConstants.API_DATE_END) && obj.containsKey(JsonConstants.API_SUMMARY)) {

							String dateBegin = obj.get(JsonConstants.API_DATE_BEGIN).toString();
							String dateEnd = obj.get(JsonConstants.API_DATE_END).toString();
							String summary = obj.get(JsonConstants.API_SUMMARY).toString();

							if (calendarNotifManager != null) {

								calendarNotifManager.getCalendarManager().createEvent(dateBegin, dateEnd, summary, new ICreateEventListener() {

									@Override
									public void onError(String description) {

										String response = "{\"error\":\"request token error\",\"error_description\":\"" + description + "\"}";

										System.out.println("[API] < " + response);

										client.sendMessage("{\"error\":\"request token error\",\"error_description\":\"" + description + "\"}");
									}

									@Override
									public void onCreateSuccess(String id) {

										String response = "{\"createEvent\":\"success\",\"eventId\":\"" + id + "\"}";

										System.out.println("[API] < " + response);

										client.sendMessage(response);
									}
								});
							}
						} else if (action.equals(JsonConstants.API_DELETE_EVENT) && obj.containsKey(JsonConstants.API_EVENT_ID)) {

							final String eventId = obj.get(JsonConstants.API_EVENT_ID).toString();

							calendarNotifManager.getCalendarManager().deleteEvent(eventId, new IDeleteEventListener() {

								@Override
								public void onSuccess() {

									String response = "{\"deleteEvent\":\"success\",\"eventId\":\"" + eventId + "\"}";

									System.out.println("[API] < " + response);

									client.sendMessage(response);
								}

								@Override
								public void onError(String description) {

									String response = "{\"error\":\"request token error\",\"error_description\":\"" + description + "\"}";

									System.out.println("[API] < " + response);

									client.sendMessage(response);
								}
							});

						} else if (action.equals(JsonConstants.API_GET_EVENTS) && obj.containsKey(JsonConstants.API_DATE_BEGIN)
								&& obj.containsKey(JsonConstants.API_DATE_END) && obj.containsKey("searchText") && calendarNotifManager != null) {

							String dateBegin = obj.get(JsonConstants.API_DATE_BEGIN).toString();
							String dateEnd = obj.get(JsonConstants.API_DATE_END).toString();
							String searchText = obj.get(JsonConstants.API_SEARCH_TEXT).toString();

							calendarNotifManager.getCalendarManager().getEventList(dateBegin, dateEnd, searchText, new IEventListListener() {

								@Override
								public void onEventListReceived(List<CalendarEvents> calendarEventList) {

									String response = "{\"eventList\":" + CalendarUtils.convertCalendarListToJsonArray(calendarEventList).toJSONString() + "}";

									System.out.println("[API] < " + response);

									client.sendMessage(response);
								}

								@Override
								public void onError(String description) {

									String response = "{\"error\":\"request token error\",\"error_description\":\"" + description + "\"}";

									System.out.println("[API] < " + response);

									client.sendMessage(response);
								}
							});

						} else if (action.equals(JsonConstants.API_SUBSCRIBE_EVENT) && obj.containsKey(JsonConstants.API_EVENT_ID)
								&& obj.containsKey(JsonConstants.API_TIME_ABOUT_TO_START)) {

							String eventId = obj.get(JsonConstants.API_EVENT_ID).toString();
							int timeAboutToStart = Integer.parseInt(obj.get(JsonConstants.API_TIME_ABOUT_TO_START).toString());

							calendarNotifManager.getNotificationManager().subscribeEvent(eventId, timeAboutToStart, new IEventListener() {

								@Override
								public void onEventStart(String eventId, String summary) {

									String response = "{\"subscribedEvent\":\"" + eventId + "\",\"eventType\":\"started\",\"summary\":\"" + summary + "\"}";

									System.out.println("[API] < " + response);

									client.sendMessage(response);
								}

								@Override
								public void onEventAboutToStart(String eventId, String summary) {

									String response = "{\"subscribedEvent\":\"" + eventId + "\",\"eventType\":\"aboutToStart\",\"summary\":\"" + summary
											+ "\"}";

									System.out.println("[API] < " + response);

									client.sendMessage(response);
								}
							});

						} else if (action.equals(JsonConstants.API_UNSUBSCRIBE_EVENT) && obj.containsKey(JsonConstants.API_EVENT_ID)) {
							String eventId = obj.get(JsonConstants.API_EVENT_ID).toString();

							calendarNotifManager.getNotificationManager().unsubscribeEvent(eventId);
						} else {
							System.out.println("[API] Error api target is inconsistent");
						}
					}
				}
			}

			@Override
			public void onClientConnection(IWebsocketClient client) {
				System.out.println("Websocket client connected");
			}

			@Override
			public void onClientClose(IWebsocketClient client) {
				System.out.println("Websocket client disconnected");
			}
		});

		Runnable websocketTask = new Runnable() {

			@Override
			public void run() {
				websocketServer.start();
			}
		};

		Thread thread = new Thread(websocketTask, "WEBSOCKET_THREAD");

		thread.start();

		server.addServerEventListener(new IHttpServerEventListener() {

			@Override
			public void onHttpFrameReceived(IHttpFrame httpFrame, HttpStates receptionStates, IHttpStream httpStream) {

				// check if http frame is OK
				if (receptionStates == HttpStates.HTTP_FRAME_OK) {

					// you can check here http frame type (response or request
					// frame)
					if (httpFrame.isHttpRequestFrame()) {

						// we want to send a message to client for http GET
						// request on page with uri /index
						if (httpFrame.getMethod().equals("GET") && httpFrame.getUri().equals("/gcalendar")) {

							String defaultPage = "";
							try {
								defaultPage = FileUtils.readFile(webPath + "/index.html", "UTF-8");
							} catch (IOException e) {
								e.printStackTrace();
							}

							// return default html page for this HTTP Server
							httpStream.writeHttpFrame(new HttpResponseFrame(StatusCodeList.OK, new HttpVersion(1, 1), new HashMap<String, String>(),
									defaultPage.getBytes()).toString().getBytes());

						} else if (httpFrame.getMethod().equals("GET") && (httpFrame.getUri().endsWith(".css") || httpFrame.getUri().endsWith(".js"))) {

							String defaultPage = "";
							try {
								defaultPage = FileUtils.readFile(webPath + httpFrame.getUri(), "UTF-8");
							} catch (IOException e) {
								e.printStackTrace();
							}

							// return default html page for this HTTP Server
							httpStream.writeHttpFrame(new HttpResponseFrame(StatusCodeList.OK, new HttpVersion(1, 1), new HashMap<String, String>(),
									defaultPage.getBytes()).toString().getBytes());
						}
					}
				}
			}
		});

		server.start();
		thread.interrupt();

	}

	/**
	 * Prints the help.
	 *
	 * @param errorDescription the error description
	 */
	private static void printHelp(String errorDescription) {

		if (!errorDescription.equals("")) {
			System.err.println("ERROR : " + errorDescription);
		}
		System.out.println("### GCALENDAR NOTIFICATION SERVER ###");
		System.out.println("Usage :");
		System.out.println("[required] webpath=<path to web folder>");
		System.out.println("[required] clientid=<google client id> (you have to retrieve it from developper console)");
		System.out.println("[required] clientsecret=<google client secret> (you have to retrieve it from developper console)");
		System.out.println("#####################################");
		System.out.println("exemple : " + "webPath=/home/abathur/Bureau/open_source/gcalendar-notification-server/web "
				+ "trustCertPath=/home/abathur/Bureau/open_source/gcalendar-notification-server/certs/trust.keystore " + "trustCertPwd=123456 "
				+ "812741506391-h38jh0j4fv0ce1krdkiq0hfvt6n5amrf.apps.googleusercontent.com " + "clientSecret=1912308409123890");

	}
}

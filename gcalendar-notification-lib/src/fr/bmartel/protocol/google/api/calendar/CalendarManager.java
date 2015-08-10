package fr.bmartel.protocol.google.api.calendar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import fr.bmartel.protocol.google.constants.GoogleConst;
import fr.bmartel.protocol.google.constants.GoogleErrorConst;
import fr.bmartel.protocol.google.oauth.device.CalendarNotifManager;
import fr.bmartel.protocol.http.ClientSocket;
import fr.bmartel.protocol.http.HttpFrame;
import fr.bmartel.protocol.http.HttpVersion;
import fr.bmartel.protocol.http.IClientSocket;
import fr.bmartel.protocol.http.IHttpClientListener;
import fr.bmartel.protocol.http.states.HttpStates;
import fr.bmartel.protocol.http.utils.ListOfBytes;

/**
 * Calendar manager used to process all Google Calendar HTTP api
 * 
 * @author Bertrand Martel
 *
 */
public class CalendarManager {

	/**
	 * authentication manager
	 */
	private CalendarNotifManager authManager = null;

	/**
	 * list of calendar events
	 */
	private List<CalendarEvents> calendarEventList = new ArrayList<CalendarEvents>();

	/**
	 * Build calendar manager
	 * 
	 * @param authManager
	 */
	public CalendarManager(CalendarNotifManager authManager) {
		this.authManager = authManager;
	}

	/**
	 * Delete a google calendar event
	 * 
	 * @param id
	 *            event id
	 * @param eventListener
	 *            event listener used to interact with api client
	 */
	public void deleteEvent(String id, final IDeleteEventListener eventListener) {
		if (authManager.getOauthRegistration() == null) {
			System.err.println("Error oauth registration not proceeded");
			return;
		}
		if (authManager.getCurrentToken() == null) {
			System.err.println("no existing current token to revoke");
			return;
		}

		String method = "DELETE";
		String uri = GoogleConst.GOOGLE_CALENDAR + "/primary" + "/events/" + id;

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Bearer " + authManager.getCurrentToken().getAccessToken());
		headers.put("Host", GoogleConst.GOOGLE_API_HOST);
		headers.put("Content-Type", "application/json ; charset=UTF-8;");

		HttpFrame frame = new HttpFrame(method, new HttpVersion(1, 1), headers, uri, new ListOfBytes(""));

		ClientSocket clientSocket = new ClientSocket(GoogleConst.GOOGLE_API_HOST, 443);

		// set SSL encryption
		clientSocket.setSsl(true);

		clientSocket.checkCertificate(false);

		clientSocket.addClientSocketEventListener(new IHttpClientListener() {

			@Override
			public void onIncomingHttpFrame(HttpFrame frame, HttpStates httpStates, IClientSocket clientSocket) {

				if (httpStates == HttpStates.HTTP_FRAME_OK && frame.isHttpResponseFrame()) {

					if (frame.getBody().getSize() > 0) {
						Object obj = JSONValue.parse(new String(frame.getBody().getBytes()));

						JSONObject mainObject = (JSONObject) obj;
						if (mainObject != null) {
							if (mainObject.containsKey(GoogleErrorConst.GOOGLE_ERROR)) {
								String description = "";

								if (mainObject.containsKey(GoogleErrorConst.GOOGLE_DESCIRPTION_ERROR)) {
									description = mainObject.get(GoogleErrorConst.GOOGLE_DESCIRPTION_ERROR).toString();
								}
								eventListener.onError(description);
								clientSocket.closeSocket();
								return;
							} else {
								eventListener.onSuccess();
							}
						}
					} else {
						eventListener.onSuccess();
					}
				}
				clientSocket.closeSocket();
			}
		});

		try {
			clientSocket.write(frame.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	/**
	 * Retrieve calendar event list
	 * 
	 * @param dateTimeBegin
	 * 		event date time start
	 * @param dateTimeEnd
	 * 		event date time end
	 * @param searchText
	 * 		filter by all field
	 * @param eventListener
	 * 		evetn listener used to interact with api client
	 */
	public void getEventList(String dateTimeBegin, String dateTimeEnd, String searchText, final IEventListListener eventListener) {
		if (authManager.getOauthRegistration() == null) {
			System.err.println("Error oauth registration not proceeded");
			eventListener.onError("not registered yet");
			return;
		}
		if (authManager.getCurrentToken() == null) {
			System.err.println("no existing current token to revoke");
			eventListener.onError("no token requested yet");
			return;
		}

		String method = "GET";
		String uri = "";

		if (searchText == null || searchText.equals("")) {
			uri = GoogleConst.GOOGLE_CALENDAR + "/primary" + "/events?timeMin=" + URLEncoder.encode(dateTimeBegin) + "&timeMax="
					+ URLEncoder.encode(dateTimeEnd);
		} else {
			uri = GoogleConst.GOOGLE_CALENDAR + "/primary" + "/events?timeMin=" + URLEncoder.encode(dateTimeBegin) + "&timeMax="
					+ URLEncoder.encode(dateTimeEnd) + "&q=" + URLEncoder.encode(searchText);
		}

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Bearer " + authManager.getCurrentToken().getAccessToken());
		headers.put("Host", GoogleConst.GOOGLE_API_HOST);
		headers.put("Content-Type", "application/json ; charset=UTF-8;");

		HttpFrame frame = new HttpFrame(method, new HttpVersion(1, 1), headers, uri, new ListOfBytes(""));

		ClientSocket clientSocket = new ClientSocket(GoogleConst.GOOGLE_API_HOST, 443);

		// set SSL encryption
		clientSocket.setSsl(true);

		clientSocket.checkCertificate(false);

		clientSocket.addClientSocketEventListener(new IHttpClientListener() {

			@Override
			public void onIncomingHttpFrame(HttpFrame frame, HttpStates httpStates, IClientSocket clientSocket) {

				if (httpStates == HttpStates.HTTP_FRAME_OK && frame.isHttpResponseFrame()) {

					if (frame.getBody().getSize() > 0) {
						Object obj = JSONValue.parse(new String(frame.getBody().getBytes()));

						try {
							JSONObject mainObject = (JSONObject) obj;
							if (mainObject != null) {
								if (mainObject.containsKey(GoogleErrorConst.GOOGLE_ERROR)) {
									String description = "";

									if (mainObject.containsKey(GoogleErrorConst.GOOGLE_DESCIRPTION_ERROR)) {
										description = mainObject.get(GoogleErrorConst.GOOGLE_DESCIRPTION_ERROR).toString();
									}
									eventListener.onError(description);
									clientSocket.closeSocket();
									return;
								} else {

									calendarEventList = new ArrayList<CalendarEvents>();

									if (mainObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_ITEMS)) {
										JSONArray eventArray = (JSONArray) mainObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_ITEMS);

										if (eventArray != null) {
											for (int i = 0; i < eventArray.size(); i++) {
												JSONObject eventObject = (JSONObject) eventArray.get(i);

												String eventId = eventObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_ID).toString();
												String eventStatus = eventObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_STATUS).toString();
												String dateCreated = eventObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_CREATED).toString();
												String dateUpdated = eventObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_UPDATED).toString();

												String summary = "";

												if (eventObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_SUMMARY)) {
													summary = eventObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_SUMMARY).toString();
												}

												JSONObject creatorObject = (JSONObject) eventObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_CREATOR);
												JSONObject organizerObject = (JSONObject) eventObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_ORGANIZER);
												JSONObject dateStartObject = (JSONObject) eventObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_START);
												JSONObject dateEndObject = (JSONObject) eventObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_END);

												String creatorEmail = "";

												if (creatorObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_EMAIL)) {
													creatorEmail = creatorObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_EMAIL).toString();
												}

												String creatorDisplayName = "";

												if (creatorObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DISPLAY_NAME)) {
													creatorDisplayName = creatorObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DISPLAY_NAME).toString();
												}

												boolean creatorIsYou = false;

												if (creatorObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_SELF)) {
													creatorIsYou = Boolean.valueOf(creatorObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_SELF).toString());
												}

												String organizerEmail = "";

												if (organizerObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_EMAIL)) {
													organizerEmail = organizerObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_EMAIL).toString();
												}

												String organizerDisplayName = "";

												if (organizerObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DISPLAY_NAME)) {
													organizerDisplayName = organizerObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DISPLAY_NAME).toString();
												}

												boolean organizerIsYou = false;

												if (organizerObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_SELF)) {
													organizerIsYou = Boolean.valueOf(organizerObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_SELF)
															.toString());
												}

												String dateStart = "";
												String dateEnd = "";
												String dateTimeStart = "";
												String dateTimeEnd = "";

												if (dateStartObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DATE)) {
													dateStart = dateStartObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DATE).toString();
												}
												if (dateEndObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DATE)) {
													dateEnd = dateEndObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DATE).toString();
												}
												if (dateStartObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DATETIME)) {
													dateTimeStart = dateStartObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DATETIME).toString();
												}
												if (dateEndObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DATETIME)) {
													dateTimeEnd = dateEndObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_DATETIME).toString();
												}

												boolean subscribed = authManager.getNotificationManager().isSubscribed(eventId);

												CalendarEvents calendarEvents = new CalendarEvents(eventId, eventStatus, dateCreated, dateUpdated, summary,
														creatorEmail, creatorDisplayName, creatorIsYou, organizerEmail, organizerDisplayName, organizerIsYou,
														dateStart, dateEnd, dateTimeStart, dateTimeEnd, subscribed);

												calendarEventList.add(calendarEvents);

											}
										}
									}
									eventListener.onEventListReceived(calendarEventList);

									clientSocket.closeSocket();
									return;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					System.err.println("Error occured while requesting user profile");
				}
				eventListener.onError("");
				clientSocket.closeSocket();
			}
		});

		try {
			clientSocket.write(frame.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create google calendar event
	 * 
	 * @param dateTimeBegin
	 *            event date time start
	 * @param dateTimeEnd
	 *            event date time end
	 * @param summary
	 *            event summary
	 * @param createEventListener
	 *            event listener used to interact with api client
	 */
	public void createEvent(String dateTimeBegin, String dateTimeEnd, String summary, final ICreateEventListener createEventListener) {

		if (authManager.getOauthRegistration() == null) {
			System.err.println("Error oauth registration not proceeded");
			createEventListener.onError("not registered yet");
			return;
		}
		if (authManager.getCurrentToken() == null) {
			System.err.println("no existing current token to revoke");
			createEventListener.onError("no token requested yet");
			return;
		}

		String calendarData = "{" + "\"kind\": \"calendar#event\"," + "\"summary\": \"" + summary + "\"," + "\"start\": {\"dateTime\": \"" + dateTimeBegin
				+ "\"},\"end\": {\"dateTime\": \"" + dateTimeEnd + "\"" + "}}";

		String method = "POST";
		String uri = GoogleConst.GOOGLE_CALENDAR + "/primary" + "/events";

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Bearer " + authManager.getCurrentToken().getAccessToken());
		headers.put("Host", GoogleConst.GOOGLE_API_HOST);
		headers.put("Content-Type", "application/json ; charset=UTF-8;");

		HttpFrame frame = new HttpFrame(method, new HttpVersion(1, 1), headers, uri, new ListOfBytes(calendarData));

		ClientSocket clientSocket = new ClientSocket(GoogleConst.GOOGLE_API_HOST, 443);

		// set SSL encryption
		clientSocket.setSsl(true);

		clientSocket.checkCertificate(false);

		clientSocket.addClientSocketEventListener(new IHttpClientListener() {

			@Override
			public void onIncomingHttpFrame(HttpFrame frame, HttpStates httpStates, IClientSocket clientSocket) {

				if (httpStates == HttpStates.HTTP_FRAME_OK && frame.isHttpResponseFrame()) {

					if (frame.getBody().getSize() > 0) {
						Object obj = JSONValue.parse(new String(frame.getBody().getBytes()));

						try {
							JSONObject mainObject = (JSONObject) obj;
							if (mainObject != null) {
								if (mainObject.containsKey(GoogleErrorConst.GOOGLE_ERROR)) {
									String description = "";

									if (mainObject.containsKey(GoogleErrorConst.GOOGLE_DESCIRPTION_ERROR)) {
										description = mainObject.get(GoogleErrorConst.GOOGLE_DESCIRPTION_ERROR).toString();
									}
									createEventListener.onError(description);
									clientSocket.closeSocket();
									return;
								} else {

									String id = "-1";

									if (mainObject.containsKey(CalendarApiConst.GOOGLE_CALENDAR_EVENT_ID)) {
										id = mainObject.get(CalendarApiConst.GOOGLE_CALENDAR_EVENT_ID).toString();
									}

									createEventListener.onCreateSuccess(id);
									clientSocket.closeSocket();
									return;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					System.err.println("Error occured while requesting user profile");
				}
				createEventListener.onError("");
				clientSocket.closeSocket();
			}
		});

		try {
			clientSocket.write(frame.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieve event summary for a specific event
	 * 
	 * @param eventId
	 * @return
	 */
	public String getEventSummary(String eventId) {
		for (int i = 0; i < calendarEventList.size(); i++) {
			if (calendarEventList.get(i).getEventId().equals(eventId)) {
				return calendarEventList.get(i).getSummary();
			}
		}
		return "";
	}

	/**
	 * retrieve google calendar event list already retrieved
	 * 
	 * @return
	 */
	public List<CalendarEvents> getCalendarEventList() {
		return calendarEventList;
	}
}

package fr.bmartel.protocol.google.api.calendar;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fr.bmartel.protocol.google.constants.CalendarConst;

/**
 * Calendar objects useful functions
 * 
 * @author Bertrand Martel
 *
 */
public class CalendarUtils {

	/**
	 * Convert calendar list to JSon array object
	 * 
	 * @param eventList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONArray convertCalendarListToJsonArray(List<CalendarEvents> eventList) {

		JSONArray array = new JSONArray();

		for (int i = 0; i < eventList.size(); i++) {
			JSONObject eventObj = new JSONObject();
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_SUMMARY, eventList.get(i).getSummary());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_DATE_START, eventList.get(i).getDateStart());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_DATE_END, eventList.get(i).getDateEnd());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_DATETIME_START, eventList.get(i).getDateTimeStart());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_DATETIME_END, eventList.get(i).getDateTimeEnd());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_DATE_CREATED, eventList.get(i).getDateCreated());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_DATE_UPDATED, eventList.get(i).getDateUpdated());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_ID, eventList.get(i).getEventId());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_STATUS, eventList.get(i).getStatus());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_CREATOR_EMAIL, eventList.get(i).getCreatorEmail());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_CREATOR_DISPALY_NAME, eventList.get(i).getCreatorDisplayName());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_ORGANIZER_EMAIL, eventList.get(i).getOrganizerEmail());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_ORGANIZER_DISPLAY_NAME, eventList.get(i).getOrganizerDisplayName());
			eventObj.put(CalendarConst.GOOGLE_CALENDAR_EVENT_SUBSCRIBED, eventList.get(i).isSubscribed());

			array.add(eventObj);
		}

		return array;
	}

	/**
	 * Retrieve date time start of a specific event in calendar list
	 * 
	 * @param calendarList
	 * @param eventId
	 * @return
	 */
	public static String getEventDateStart(List<CalendarEvents> calendarList, String eventId) {
		for (int i = 0; i < calendarList.size(); i++) {
			if (calendarList.get(i).getEventId().equals(eventId)) {
				return calendarList.get(i).getDateTimeStart();
			}
		}
		return "";
	}
}

package fr.bmartel.protocol.google.api.calendar;

/**
 * Calendar object
 * 
 * https://developers.google.com/google-apps/calendar/v3/reference/
 * 
 * @author Bertrand Martel
 *
 */
public class CalendarObject {

	/**
	 * calendar id
	 */
	private String calendarId = "";

	/**
	 * calendar timezone
	 */
	private String timezone = "";

	/**
	 * build calendar object
	 * 
	 * @param calendarId
	 * @param timezone
	 */
	public CalendarObject(String calendarId, String timezone) {
		this.calendarId = calendarId;
		this.timezone = timezone;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public String getTimezone() {
		return timezone;
	}
}

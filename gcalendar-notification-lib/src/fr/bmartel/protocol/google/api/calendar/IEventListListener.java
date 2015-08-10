package fr.bmartel.protocol.google.api.calendar;

import java.util.List;

/**
 * Listener for getCalendarEventList API
 * 
 * @author Bertrand Martel
 *
 */
public interface IEventListListener {

	/**
	 * called when calendar event list has successfully beeen retrieved
	 * 
	 * @param calendarEventList
	 */
	public void onEventListReceived(List<CalendarEvents> calendarEventList);

	/**
	 * called when error occured retrieving calendar event list
	 * 
	 * @param description
	 */
	public void onError(String description);
}

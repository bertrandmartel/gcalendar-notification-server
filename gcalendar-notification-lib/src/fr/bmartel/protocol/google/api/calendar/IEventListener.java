package fr.bmartel.protocol.google.api.calendar;

/**
 * Listener for scheduled event task (for notification purpose)
 * 
 * @author Bertrand Martel
 *
 */
public interface IEventListener {

	/**
	 * called when event is about to start
	 * 
	 * @param eventId
	 * @param eventSummary
	 */
	public void onEventAboutToStart(String eventId, String eventSummary);

	/**
	 * called when event has started
	 * 
	 * @param eventId
	 * @param eventSummary
	 */
	public void onEventStart(String eventId, String eventSummary);

}

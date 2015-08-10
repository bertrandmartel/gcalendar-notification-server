package fr.bmartel.protocol.google.api.calendar;

/**
 * Listener for creation of calendar event
 * 
 * @author Bertrand Martel
 *
 */
public interface ICreateEventListener {

	/**
	 * called when event has been successfully created
	 * 
	 * @param id
	 */
	public void onCreateSuccess(String id);

	/**
	 * called when error occurred during creation of event
	 * 
	 * @param description
	 */
	public void onError(String description);
}

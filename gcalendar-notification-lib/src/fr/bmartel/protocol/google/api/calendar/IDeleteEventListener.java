package fr.bmartel.protocol.google.api.calendar;

/**
 * Listener for event deletion
 * 
 * @author Bertrand Martel
 *
 */
public interface IDeleteEventListener {

	/**
	 * called when event has been deleted successfully
	 */
	public void onSuccess();

	/**
	 * called when error occurred during event deletion
	 * 
	 * @param description
	 */
	public void onError(String description);
}

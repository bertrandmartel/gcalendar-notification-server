package fr.bmartel.protocol.google.api.profile;

/**
 * Listener for user profile api call
 * 
 * @author Bertrand Martel
 *
 */
public interface IUserProfileListener {

	/**
	 * called when user profile has been retrieved
	 * 
	 * @param userProfile
	 */
	public void onSuccess(UserProfile userProfile);

	/**
	 * called when error occurred retrieving user profile
	 * 
	 * @param description
	 */
	public void onError(String description);
}

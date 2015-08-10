package fr.bmartel.protocol.google.oauth.device;

/**
 * Request token listener
 * 
 * @author Bertrand Martel
 *
 */
public interface IRequestTokenListener {

	/**
	 * called when request token have successfully been retrieved
	 * 
	 * @param token
	 */
	public void onRequestTokenReceived(OauthToken token);

	/**
	 * called when error occured retrieveing access token
	 * 
	 * @param description
	 */
	public void onRequestTokenError(String description);

}

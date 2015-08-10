package fr.bmartel.protocol.google.oauth.device;

/**
 * Revoke token listener
 * 
 * @author Bertrand Martel
 *
 */
public interface IRevokeTokenListener {

	/**
	 * called when token revocation is successful
	 */
	public void onSuccess();

	/**
	 * called when error occurred during token revocation
	 * 
	 * @param description
	 */
	public void onError(String description);
}

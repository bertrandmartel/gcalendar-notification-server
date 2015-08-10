package fr.bmartel.protocol.google.oauth.device;

/**
 * Listener for oauth API for device response
 * 
 * @author Bertrand Martel
 *
 */
public interface IOauthDeviceResponseListener {

	/**
	 * called when response is received with verification url and usercode
	 */
	public void onResponseReceived(OauthForDeviceResponse response);

}

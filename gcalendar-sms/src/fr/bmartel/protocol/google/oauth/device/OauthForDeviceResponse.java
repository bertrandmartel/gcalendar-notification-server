package fr.bmartel.protocol.google.oauth.device;

import org.json.simple.JSONObject;

import fr.bmartel.protocol.google.constants.OauthGoogleConstants;
import fr.bmartel.utils.CommonUtils;

/**
 * This features decoder for Google Oauth2.0 for device response
 * 
 * @author Bertrand Martel
 *
 */
public class OauthForDeviceResponse {

	/**
	 * device code is used to obtain access token and refresh token
	 */
	private String deviceCode = "";

	/**
	 * use code is the code the user have to enter at the url specified by
	 * verification url
	 */
	private String userCode = "";

	/**
	 * url where the user has to enter the userCOde
	 */
	private String verificationUrl = "";

	/**
	 * expiring before value used to obtain access token and refresh token
	 */
	private int expiringBefore = -1;

	/**
	 * interval used to obtain access token and refresh token
	 */
	private int interval = 0;

	/**
	 * Build Oauth2.0 device response
	 * 
	 * @param deviceCode
	 * @param userCode
	 * @param url
	 * @param expiring
	 * @param interval
	 */
	public OauthForDeviceResponse(String deviceCode, String userCode,
			String url, int expiring, int interval) {
		this.setDeviceCode(deviceCode);
		this.setUserCode(userCode);
		this.setVerificationUrl(url);
		this.setExpiringBefore(expiring);
		this.setInterval(interval);
	}

	public static OauthForDeviceResponse decodeOauthForDeviceResponse(
			JSONObject response) {
		if (response != null) {
			if (response
					.containsKey(OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_DEVICE_CODE)
					&& response
							.containsKey(OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_USER_CODE)
					&& response
							.containsKey(OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_VERIFICATION_URL)
					&& response
							.containsKey(OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_EXPIRING)
					&& CommonUtils
							.isInteger(response
									.get(OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_EXPIRING)
									.toString())
					&& response
							.containsKey(OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_INTERVAL)
					&& CommonUtils
							.isInteger(response
									.get(OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_INTERVAL)
									.toString())) {

				// build oauth device response
				return new OauthForDeviceResponse(
						response.get(
								OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_DEVICE_CODE)
								.toString(),
						response.get(
								OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_USER_CODE)
								.toString(),
						response.get(
								OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_VERIFICATION_URL)
								.toString(),
						Integer.parseInt(response
								.get(OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_EXPIRING)
								.toString()),
						Integer.parseInt(response
								.get(OauthGoogleConstants.OAUTH_DEVICE_RESPONSE_INTERVAL)
								.toString()));
			}
		}
		return null;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getVerificationUrl() {
		return verificationUrl;
	}

	public void setVerificationUrl(String verificationUrl) {
		this.verificationUrl = verificationUrl;
	}

	public int getExpiringBefore() {
		return expiringBefore;
	}

	public void setExpiringBefore(int expiringBefore) {
		this.expiringBefore = expiringBefore;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public void displayInfo() {
		System.out.println("");
		System.out.println("device code      => " + deviceCode);
		System.out.println("user   code      => " + userCode);
		System.out.println("verification url => " + verificationUrl);
		System.out.println("expiring date    => " + expiringBefore);
		System.out.println("interval         => " + interval);
	}
}
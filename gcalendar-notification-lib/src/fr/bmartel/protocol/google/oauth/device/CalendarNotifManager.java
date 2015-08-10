/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Bertrand Martel
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.protocol.google.oauth.device;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import fr.bmartel.protocol.google.api.calendar.CalendarManager;
import fr.bmartel.protocol.google.api.profile.UserProfileManager;
import fr.bmartel.protocol.google.constants.GoogleConst;
import fr.bmartel.protocol.google.constants.GoogleScopes;
import fr.bmartel.protocol.google.constants.GoogleScopes.DeviceScope;
import fr.bmartel.protocol.google.notification.NotificationManager;
import fr.bmartel.protocol.http.ClientSocket;
import fr.bmartel.protocol.http.HttpFrame;
import fr.bmartel.protocol.http.HttpVersion;
import fr.bmartel.protocol.http.IClientSocket;
import fr.bmartel.protocol.http.IHttpClientListener;
import fr.bmartel.protocol.http.states.HttpStates;
import fr.bmartel.protocol.http.utils.ListOfBytes;

/**
 * Manager class monitoring all google authentication process and manage api
 * processing
 * 
 * @author Bertrand Martel
 *
 */
public class CalendarNotifManager {

	/**
	 * Oauth2.0 client Id generated from google console
	 */
	private String clientId = "";

	/**
	 * Oaut2.0 client secret generated from google console
	 */
	private String clientSecret = "";

	/**
	 * oauth current token
	 */
	private OauthToken currentToken = null;

	/**
	 * oauth registration object (retrieving verification url / user code /
	 * device code / user code life time ...)
	 */
	private OauthForDeviceResponse oauthRegistration = null;

	/**
	 * calendar manager
	 */
	private CalendarManager calendarManager = null;

	/**
	 * User profile manager
	 */
	private UserProfileManager userProfileManager = null;

	/**
	 * Notification manager
	 */
	private NotificationManager notificationManager = null;

	/**
	 * Scope list the device will be granted authorization to access on google
	 * account
	 */
	final static private ArrayList<DeviceScope> scopeList = new ArrayList<GoogleScopes.DeviceScope>();

	static {
		scopeList.add(DeviceScope.PROFILE);
		scopeList.add(DeviceScope.CALENDAR);
	}

	/**
	 * Build Authentication Manager
	 * 
	 * @param clientId
	 *            Oauth2.0 client Id generated from google console
	 * @param scopeList
	 *            Scope list the device will be granted authorization to access
	 *            on google account
	 */
	public CalendarNotifManager(String clientId, String clientSecret) {

		this.clientId = clientId;
		this.clientSecret = clientSecret;

		calendarManager = new CalendarManager(this);
		userProfileManager = new UserProfileManager(this);
		notificationManager = new NotificationManager(this);
	}

	/**
	 * Request usercode and verification url
	 * 
	 */
	public void requestDeviceAuth(final IOauthDeviceResponseListener responseListener) {

		String method = "POST";
		String uri = GoogleConst.GOOGLE_USERCODE_REQUEST_URI;
		String requestBody = "client_id=" + this.clientId + "&" + "scope=" + convertScopeToStr();

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Host", GoogleConst.GOOGLE_ACCOUNT_HOST);

		HttpFrame frame = new HttpFrame(method, new HttpVersion(1, 1), headers, uri, new ListOfBytes(requestBody));

		ClientSocket clientSocket = new ClientSocket(GoogleConst.GOOGLE_ACCOUNT_HOST, 443);

		// set SSL encryption
		clientSocket.setSsl(true);

		clientSocket.checkCertificate(false);

		clientSocket.addClientSocketEventListener(new IHttpClientListener() {

			@Override
			public void onIncomingHttpFrame(HttpFrame frame, HttpStates httpStates, IClientSocket clientSocket) {

				if (httpStates == HttpStates.HTTP_FRAME_OK && frame.isHttpResponseFrame()) {

					Object obj = JSONValue.parse(new String(frame.getBody().getBytes()));

					JSONObject queryResponse = (JSONObject) obj;

					if (queryResponse != null) {

						oauthRegistration = OauthForDeviceResponse.decodeOauthForDeviceResponse(queryResponse);

						if (responseListener != null) {
							responseListener.onResponseReceived(oauthRegistration);
						}
					}

				}
				clientSocket.closeSocket();
			}
		});

		clientSocket.write(frame.toString().getBytes());
	}

	public OauthForDeviceResponse getOauthRegistration() {
		return oauthRegistration;
	}

	public OauthToken getCurrentToken() {
		return currentToken;
	}

	/**
	 * convert scope list to string for request
	 * 
	 * @return
	 */
	private String convertScopeToStr() {
		if (scopeList.size() > 0) {
			String ret = "";

			for (int i = 0; i < scopeList.size(); i++) {
				ret += GoogleScopes.getDeviceScope(scopeList.get(i));
				if (i != scopeList.size() - 1)
					ret += "%20";
			}
			return ret;
		}
		return "";
	}

	public void revokeToken(final IRevokeTokenListener revokeTokenListener) {
		if (oauthRegistration == null) {
			System.err.println("Error oauth registration not proceeded");
			revokeTokenListener.onError("no registration has been processed yet");
			return;
		}
		if (currentToken == null) {
			System.err.println("no existing current token to revoke");
			revokeTokenListener.onError("no token has been requested yet");
			return;
		}

		String method = "GET";
		String uri = GoogleConst.GOOGLE_REVOKE_TOKEN + "?token=" + currentToken.getAccessToken();

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		headers.put("Host", GoogleConst.GOOGLE_ACCOUNT_HOST);
		headers.put("Accept", "*/*");
		headers.put("Accept-Encoding", "gzip, deflate, compress");

		String body = "";

		HttpFrame frame = new HttpFrame(method, new HttpVersion(1, 1), headers, uri, new ListOfBytes(body));

		ClientSocket clientSocket = new ClientSocket(GoogleConst.GOOGLE_ACCOUNT_HOST, 443);

		// set SSL encryption
		clientSocket.setSsl(true);

		clientSocket.checkCertificate(false);

		clientSocket.addClientSocketEventListener(new IHttpClientListener() {

			@Override
			public void onIncomingHttpFrame(HttpFrame frame, HttpStates httpStates, IClientSocket clientSocket) {

				if (httpStates == HttpStates.HTTP_FRAME_OK && frame.isHttpResponseFrame()) {

					if (frame.getBody() != null && frame.getBody().getSize() > 0) {

						Object obj = JSONValue.parse(new String(frame.getBody().getBytes()));

						try {
							JSONObject mainObject = (JSONObject) obj;
							if (mainObject.containsKey("error")) {
								System.err.println("revoke token error. Retry later ...");

								if (mainObject.containsKey("error_description")) {
									revokeTokenListener.onError(mainObject.get("error_description").toString());
									clientSocket.closeSocket();
									return;
								}
							} else {
								currentToken = null;
								revokeTokenListener.onSuccess();
								clientSocket.closeSocket();
								return;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else {
						currentToken = null;
						revokeTokenListener.onSuccess();
						clientSocket.closeSocket();
						return;
					}
				} else {

					System.err.println("Error occured while requesting token");
				}
				clientSocket.closeSocket();
				revokeTokenListener.onError("");
			}
		});
		clientSocket.write(frame.toString().getBytes());
	}

	public void requestToken(final IRequestTokenListener requestTokenListener) {

		if (oauthRegistration == null) {
			System.err.println("Error oauth registration not proceeded");
			requestTokenListener.onRequestTokenError("oauth registration must be proccessed before");
			return;
		}

		String method = "POST";
		String uri = GoogleConst.GOOGLE_TOKEN;

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		headers.put("Host", GoogleConst.GOOGLE_ACCOUNT_HOST);
		headers.put("Accept", "*/*");
		headers.put("Accept-Encoding", "gzip, deflate, compress");

		String body = "client_id=" + clientId + "&" + "client_secret=" + clientSecret + "&" + "code=" + oauthRegistration.getDeviceCode() + "&"
				+ "grant_type=http://oauth.net/grant_type/device/1.0";

		HttpFrame frame = new HttpFrame(method, new HttpVersion(1, 1), headers, uri, new ListOfBytes(body));

		ClientSocket clientSocket = new ClientSocket(GoogleConst.GOOGLE_ACCOUNT_HOST, 443);

		// set SSL encryption
		clientSocket.setSsl(true);

		clientSocket.checkCertificate(false);

		clientSocket.addClientSocketEventListener(new IHttpClientListener() {

			@Override
			public void onIncomingHttpFrame(HttpFrame frame, HttpStates httpStates, IClientSocket clientSocket) {

				if (httpStates == HttpStates.HTTP_FRAME_OK && frame.isHttpResponseFrame()) {

					Object obj = JSONValue.parse(new String(frame.getBody().getBytes()));

					try {
						JSONObject mainObject = (JSONObject) obj;
						if (mainObject.containsKey("error")) {
							System.err.println("request token error. Retry later ...");
							String description = "";
							if (mainObject.containsKey("error_description")) {
								description = mainObject.get("error_description").toString();
							}
							requestTokenListener.onRequestTokenError(description);
						} else {
							if (mainObject.containsKey("access_token") == true && mainObject.containsKey("token_type") == true
									&& mainObject.containsKey("expires_in") == true) {

								currentToken = new OauthToken(mainObject.get("access_token").toString(), mainObject.get("token_type").toString(), Integer
										.parseInt(mainObject.get("expires_in").toString()));

								requestTokenListener.onRequestTokenReceived(currentToken);

								return;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {

					System.err.println("Error occured while requesting token");
				}
				requestTokenListener.onRequestTokenError("");
			}
		});
		clientSocket.write(frame.toString().getBytes());
	}

	public UserProfileManager getUserProfileManager() {
		return userProfileManager;
	}

	public CalendarManager getCalendarManager() {
		return calendarManager;
	}

	public NotificationManager getNotificationManager() {
		return notificationManager;
	}
}

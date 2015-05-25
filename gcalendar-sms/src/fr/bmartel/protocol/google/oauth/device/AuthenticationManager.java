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
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import fr.bmartel.protocol.google.constants.GoogleConst;
import fr.bmartel.protocol.google.constants.GoogleScopes;
import fr.bmartel.protocol.google.constants.GoogleScopes.DeviceScope;
import fr.bmartel.protocol.http.ClientSocket;
import fr.bmartel.protocol.http.HttpFrame;
import fr.bmartel.protocol.http.HttpVersion;
import fr.bmartel.protocol.http.IClientSocket;
import fr.bmartel.protocol.http.IHttpClientListener;
import fr.bmartel.protocol.http.inter.IHttpFrame;
import fr.bmartel.protocol.http.states.HttpStates;
import fr.bmartel.protocol.http.utils.ListOfBytes;

/**
 * Manager class monitoring all google authentication process
 * 
 * @author Bertrand Martel
 *
 */
public class AuthenticationManager {

	/**
	 * Oauth2.0 client Id generated from google console
	 */
	private String clientId = "";

	/**
	 * Scope list the device will be granted authorization to access on google
	 * account
	 */
	private ArrayList<DeviceScope> scopeList = new ArrayList<GoogleScopes.DeviceScope>();

	/**
	 * Build Authentication Manager
	 * 
	 * @param clientId
	 *            Oauth2.0 client Id generated from google console
	 * @param scopeList
	 *            Scope list the device will be granted authorization to access
	 *            on google account
	 */
	public AuthenticationManager(String clientId,
			ArrayList<DeviceScope> scopeList) {
		this.clientId = clientId;

		// put profile as default scope to retrieve at least something
		if (scopeList.size() > 0)
			this.scopeList = scopeList;
		else
			scopeList.add(DeviceScope.PROFILE);
	}

	/**
	 * Request usercode and verification url
	 * 
	 */
	public void requestDeviceAuth(
			final IOauthDeviceResponseListener responseListener) {

		String method = "POST";
		String uri = GoogleConst.GOOGLE_USERCODE_REQUEST_URI;
		String requestBody = "client_id=" + this.clientId + "&" + "scope="
				+ convertScopeToStr();

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Host", GoogleConst.GOOGLE_ACCOUNT_HOST);

		HttpFrame frame = new HttpFrame(method, new HttpVersion(1, 1), headers,
				uri, new ListOfBytes(requestBody));

		ClientSocket clientSocket = new ClientSocket(
				GoogleConst.GOOGLE_ACCOUNT_HOST, 443);

		// set SSL encryption
		clientSocket.setSsl(true);

		// set ssl parameters

		clientSocket
				.setSSLParams(
						"JKS",
						"JKS",
						"",
						"/home/abathur/Bureau/open_source/google-sms-gcalendar/certs/trust.keystore",
						"TLS", "", "123456");

		clientSocket.addClientSocketEventListener(new IHttpClientListener() {

			@Override
			public void onIncomingHttpFrame(HttpFrame frame,
					HttpStates httpStates, IClientSocket clientSocket) {

				if (httpStates == HttpStates.HTTP_FRAME_OK
						&& frame.isHttpResponseFrame()) {

					Object obj = JSONValue.parse(new String(frame.getBody()
							.getBytes()));

					JSONObject queryResponse = (JSONObject) obj;

					if (queryResponse != null) {
						
						OauthForDeviceResponse response = OauthForDeviceResponse
								.decodeOauthForDeviceResponse(queryResponse);
						
						if (responseListener != null) {
							responseListener.onResponseReceived(response);
						}
					}

				}

			}
		});

		clientSocket.write(frame.toString().getBytes());
	}

	/**
	 * Print result of http decoding
	 * 
	 * @param frame
	 *            http frame object
	 * @param decodingStates
	 *            final states of http decoding (to catch http decoding error)
	 */
	public static void printHttpFrameDecodedResult(IHttpFrame frame,
			HttpStates decodingStates) {
		if (frame.isHttpRequestFrame()) {
			System.out.println("uri         : " + frame.getUri());
			System.out.println("version     : " + frame.getHttpVersion());
			System.out.println("method      : " + frame.getMethod());
			System.out.println("querystring : " + frame.getQueryString());
			System.out.println("hosy        : " + frame.getHost());
			System.out.println("body        : "
					+ new String(frame.getBody().getBytes()));

			Set<String> keys = frame.getHeaders().keySet();
			Iterator<String> it = keys.iterator();
			int count = 0;
			while (it.hasNext()) {
				Object key = it.next();
				Object value = frame.getHeaders().get(key);
				System.out.println("headers n ° " + count + " : "
						+ key.toString() + " => " + value.toString());
			}
		} else if (frame.isHttpResponseFrame()) {
			System.out
					.println("status code         : " + frame.getStatusCode());
			System.out.println("reason phrase       : "
					+ frame.getReasonPhrase());
			System.out.println("body                : "
					+ new String(frame.getBody().getBytes()));

			Set<String> keys = frame.getHeaders().keySet();
			Iterator<String> it = keys.iterator();
			int count = 0;
			while (it.hasNext()) {
				Object key = it.next();
				Object value = frame.getHeaders().get(key);
				System.out.println("headers n ° " + count + " : "
						+ key.toString() + " => " + value.toString());
			}
		} else {
			System.out
					.println("Error, this http frame has not beed decoded correctly. Error code : "
							+ decodingStates.toString());
		}
		System.out.println("##########################################");
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
					ret += "&";
			}
			return ret;
		}
		return "";
	}
}

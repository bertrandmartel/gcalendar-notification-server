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
package fr.bmartel.protocol.google.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.bmartel.protocol.google.constants.GoogleScopes;
import fr.bmartel.protocol.google.constants.GoogleScopes.DeviceScope;
import fr.bmartel.protocol.google.oauth.device.AuthenticationManager;
import fr.bmartel.protocol.google.oauth.device.IOauthDeviceResponseListener;
import fr.bmartel.protocol.google.oauth.device.OauthForDeviceResponse;
import fr.bmartel.protocol.http.HttpResponseFrame;
import fr.bmartel.protocol.http.HttpVersion;
import fr.bmartel.protocol.http.constants.StatusCodeList;
import fr.bmartel.protocol.http.inter.IHttpFrame;
import fr.bmartel.protocol.http.listeners.IHttpServerEventListener;
import fr.bmartel.protocol.http.server.HttpServer;
import fr.bmartel.protocol.http.server.IHttpStream;
import fr.bmartel.protocol.http.states.HttpStates;

/**
 * Trigger Google authentication test
 * 
 * User is invited to enter his gmail address and will be
 * authenticated/connected without User Interface
 * 
 * @author Bertrand Martel
 *
 */
public class LaunchOauthApiServer {

	private final static int PORT = 4242;

	/**
	 * Invite user to enter google email address
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		FileInputStream clientIdFis;
		StringBuilder clientId = new StringBuilder();

		try {
			clientIdFis = new FileInputStream(
					"/home/abathur/Bureau/GOOGLE_CLIENT_ID.txt");
			int ch;
			while ((ch = clientIdFis.read()) != -1) {
				clientId.append((char) ch);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Scope list the device will be granted authorization to access on
		// google account
		final ArrayList<GoogleScopes.DeviceScope> deviceScopeList = new ArrayList<GoogleScopes.DeviceScope>();
		deviceScopeList.add(DeviceScope.PROFILE);
		deviceScopeList.add(DeviceScope.GOOGLE_PLUS);
		deviceScopeList.add(DeviceScope.CALENDAR);
		deviceScopeList.add(DeviceScope.EMAIL);

		final String clientIdStr = clientId.toString();

		// start http server
		HttpServer server = new HttpServer(PORT);

		server.addServerEventListener(new IHttpServerEventListener() {

			@Override
			public void onHttpFrameReceived(IHttpFrame httpFrame,
					HttpStates receptionStates, IHttpStream httpStream) {

				// check if http frame is OK
				if (receptionStates == HttpStates.HTTP_FRAME_OK) {

					// you can check here http frame type (response or request
					// frame)
					if (httpFrame.isHttpRequestFrame()) {

						// we want to send a message to client for http GET
						// request on page with uri /index
						if (httpFrame.getMethod().equals("GET")
								&& httpFrame.getUri().equals("/index")) {

							HashMap<String, String> headers = new HashMap<String, String>();

							String defaultPage = "Hello from custom Java HTTP Server\r\nThis page has been seen "
									+ " times before.";

							// return default html page for this HTTP Server
							httpStream.writeHttpFrame(new HttpResponseFrame(
									StatusCodeList.OK, new HttpVersion(1, 1),
									headers, defaultPage.getBytes()).toString()
									.getBytes());
						} else if (httpFrame.getMethod().equals("GET")
								&& httpFrame.getUri().equals("/oauthPage")) {

							AuthenticationManager googleAuthManager = new AuthenticationManager(
									clientIdStr, deviceScopeList);

							googleAuthManager
									.requestDeviceAuth(new IOauthDeviceResponseListener() {

										@Override
										public void onResponseReceived(
												OauthForDeviceResponse response) {
											if (response != null) {
												response.displayInfo();
											}
										}
									});
						}
					}
				}
			}
		});

		server.start();
	}
}

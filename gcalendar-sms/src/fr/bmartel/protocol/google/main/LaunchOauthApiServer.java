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

import fr.bmartel.protocol.google.constants.GoogleScopes;
import fr.bmartel.protocol.google.constants.GoogleScopes.DeviceScope;
import fr.bmartel.protocol.google.oauth.device.AuthenticationManager;

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

	/**
	 * Invite user to enter google email address
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		FileInputStream clientIdFis;
		StringBuilder builder = new StringBuilder();

		try {
			clientIdFis = new FileInputStream(
					"/home/abathur/Bureau/GOOGLE_CLIENT_ID.txt");
			int ch;
			while ((ch = clientIdFis.read()) != -1) {
				builder.append((char) ch);
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
		ArrayList<GoogleScopes.DeviceScope> deviceScopeList = new ArrayList<GoogleScopes.DeviceScope>();
		deviceScopeList.add(DeviceScope.PROFILE);
		deviceScopeList.add(DeviceScope.GOOGLE_PLUS);
		deviceScopeList.add(DeviceScope.CALENDAR);
		deviceScopeList.add(DeviceScope.EMAIL);

		AuthenticationManager googleAuthManager = new AuthenticationManager(
				builder.toString(), deviceScopeList);

		googleAuthManager.requestDeviceAuth();
	}
}

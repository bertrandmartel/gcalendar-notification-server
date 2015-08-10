package fr.bmartel.protocol.google.api.profile;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import fr.bmartel.protocol.google.constants.GoogleConst;
import fr.bmartel.protocol.google.constants.GoogleErrorConst;
import fr.bmartel.protocol.google.oauth.device.CalendarNotifManager;
import fr.bmartel.protocol.http.ClientSocket;
import fr.bmartel.protocol.http.HttpFrame;
import fr.bmartel.protocol.http.HttpVersion;
import fr.bmartel.protocol.http.IClientSocket;
import fr.bmartel.protocol.http.IHttpClientListener;
import fr.bmartel.protocol.http.states.HttpStates;
import fr.bmartel.protocol.http.utils.ListOfBytes;

/**
 * User profile manager used to process User profile API
 * 
 * @author Bertrand Martel
 *
 */
public class UserProfileManager {

	/**
	 * Authentication manager
	 */
	private CalendarNotifManager authManager = null;

	/**
	 * Build user profile manager
	 * 
	 * @param authManager
	 */
	public UserProfileManager(CalendarNotifManager authManager) {
		this.authManager = authManager;
	}

	/**
	 * Retrieve user profile through google HTTP api
	 * 
	 * @param userProfileListener
	 */
	public void getUserProfile(final IUserProfileListener userProfileListener) {

		if (authManager.getOauthRegistration() == null) {
			System.err.println("Error oauth registration not proceeded");
			userProfileListener.onError("no registration has been processed yet");
			return;
		}
		if (authManager.getCurrentToken() == null) {
			System.err.println("no existing current token to revoke");
			userProfileListener.onError("no token has been requested yet");
			return;
		}

		String method = "GET";
		String uri = GoogleConst.GOOGLE_PROFILE;

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Bearer " + authManager.getCurrentToken().getAccessToken());
		headers.put("Host", GoogleConst.GOOGLE_API_HOST);

		HttpFrame frame = new HttpFrame(method, new HttpVersion(1, 1), headers, uri, new ListOfBytes(""));

		ClientSocket clientSocket = new ClientSocket(GoogleConst.GOOGLE_API_HOST, 443);

		clientSocket.checkCertificate(false);

		// set SSL encryption
		clientSocket.setSsl(true);

		clientSocket.addClientSocketEventListener(new IHttpClientListener() {

			@Override
			public void onIncomingHttpFrame(HttpFrame frame, HttpStates httpStates, IClientSocket clientSocket) {

				if (httpStates == HttpStates.HTTP_FRAME_OK && frame.isHttpResponseFrame()) {

					if (frame.getBody().getSize() > 0) {
						Object obj = JSONValue.parse(new String(frame.getBody().getBytes()));

						try {
							JSONObject mainObject = (JSONObject) obj;
							if (mainObject != null) {
								if (mainObject.containsKey(GoogleErrorConst.GOOGLE_ERROR)) {
									String description = "";

									if (mainObject.containsKey(GoogleErrorConst.GOOGLE_DESCIRPTION_ERROR)) {
										description = mainObject.get(GoogleErrorConst.GOOGLE_DESCIRPTION_ERROR).toString();
									}
									userProfileListener.onError(description);
									clientSocket.closeSocket();
									return;

								} else {
									JSONObject nameArray = (JSONObject) mainObject.get(UserProfileConst.GOOGLE_USER_PROFILE_NAME_ARRAY);

									String gender = "";
									String displayName = "";
									String familyName = "";
									String givenName = "";
									String language = "";

									if (mainObject.containsKey(UserProfileConst.GOOGLE_USER_PROFILE_GENDER))
										gender = mainObject.get(UserProfileConst.GOOGLE_USER_PROFILE_GENDER).toString();

									if (mainObject.containsKey(UserProfileConst.GOOGLE_USER_PROFILE_DISPLAY_NAME))
										displayName = mainObject.get(UserProfileConst.GOOGLE_USER_PROFILE_DISPLAY_NAME).toString();

									if (nameArray.containsKey(UserProfileConst.GOOGLE_USER_PROFILE_FAMILY_NAME))
										familyName = nameArray.get(UserProfileConst.GOOGLE_USER_PROFILE_FAMILY_NAME).toString();

									if (nameArray.containsKey(UserProfileConst.GOOGLE_USER_PROFILE_GIVEN_NAME))
										givenName = nameArray.get(UserProfileConst.GOOGLE_USER_PROFILE_GIVEN_NAME).toString();

									if (mainObject.containsKey(UserProfileConst.GOOGLE_USER_PROFILE_LANGUAGE))
										language = mainObject.get(UserProfileConst.GOOGLE_USER_PROFILE_LANGUAGE).toString();

									UserProfile profile = new UserProfile(gender, displayName, familyName, givenName, language);

									userProfileListener.onSuccess(profile);
									clientSocket.closeSocket();
									return;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					System.err.println("Error occured while requesting user profile");
				}
				userProfileListener.onError("");
				clientSocket.closeSocket();
			}
		});

		try {
			clientSocket.write(frame.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}

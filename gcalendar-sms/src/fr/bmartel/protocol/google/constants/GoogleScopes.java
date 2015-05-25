package fr.bmartel.protocol.google.constants;

/**
 * List of scopes available for Oauth2.0 device authentication
 * 
 * Source : https://developers.google.com/identity/protocols/OAuth2ForDevices#allowedscopes
 * 
 * 
 * @author Bertrand Martel
 *
 */
public class GoogleScopes {

	/**
	 * full scopes relative to Device 
	 * 
	 * @author Bertrand Martel
	 *
	 */
	public enum DeviceScope {
		PROFILE, 
		EMAIL, 
		GOOGLE_PLUS, 
		CALENDAR, CALENDAR_READ_ONLY, 
		CONTACTS_READ_ONLY, 
		CLOUD_PRINT, 
		YOUTUBE, YOUTUBE_READ_ONLY, YOUTUBE_UPLOAD, 
		FUSIONTABLES, 
		ANALYTICS,ANALYTICS_READ_ONLY,
		NONE
	}
	
	/**
	 * Convert scope enum to url parameter string
	 * 
	 * check https://developers.google.com/identity/protocols/OAuth2ForDevices#allowedscopes
	 * 
	 * @param scope
	 * @return
	 */
	public static String getDeviceScope(DeviceScope scope)
	{
		switch (scope)
		{
		case PROFILE:
			return "profile";
		case EMAIL:
			return "email";
		case GOOGLE_PLUS:
			return "https://www.googleapis.com/auth/plus.me";
		case CALENDAR:
			return "https://www.googleapis.com/auth/calendar";
		case CALENDAR_READ_ONLY:
			return "https://www.googleapis.com/auth/calendar.readonly";
		case CLOUD_PRINT:
			return "https://www.googleapis.com/auth/cloudprint";
		case YOUTUBE:
			return "https://www.googleapis.com/auth/youtube";
		case YOUTUBE_READ_ONLY:
			return "https://www.googleapis.com/auth/youtube.readonly";
		case YOUTUBE_UPLOAD:
			return "https://www.googleapis.com/auth/youtube.upload";
		case FUSIONTABLES:
			return "https://www.googleapis.com/auth/fusiontables";
		case ANALYTICS:
			return "https://www.googleapis.com/auth/analytics";
		case ANALYTICS_READ_ONLY:
			return "https://www.googleapis.com/auth/analytics.readonly";
		default:
			return "";
		}
	}
	
	/**
	 * Convert scope url parameter to enum 
	 * 
	 * check https://developers.google.com/identity/protocols/OAuth2ForDevices#allowedscopes
	 * 
	 * @param scope
	 * @return
	 */
	public static DeviceScope getDeviceScope(String scope)
	{
		switch (scope)
		{
		case "profile":
			return DeviceScope.PROFILE;
		case "email":
			return DeviceScope.EMAIL;
		case "https://www.googleapis.com/auth/plus.me":
			return DeviceScope.GOOGLE_PLUS;
		case "https://www.googleapis.com/auth/calendar":
			return DeviceScope.CALENDAR;
		case "https://www.googleapis.com/auth/calendar.readonly":
			return DeviceScope.CALENDAR_READ_ONLY;
		case "https://www.googleapis.com/auth/cloudprint":
			return DeviceScope.CLOUD_PRINT;
		case "https://www.googleapis.com/auth/youtube":
			return DeviceScope.YOUTUBE;
		case "https://www.googleapis.com/auth/youtube.readonly":
			return DeviceScope.YOUTUBE_READ_ONLY;
		case "https://www.googleapis.com/auth/youtube.upload":
			return DeviceScope.YOUTUBE_UPLOAD;
		case "https://www.googleapis.com/auth/fusiontables":
			return DeviceScope.FUSIONTABLES;
		case "https://www.googleapis.com/auth/analytics":
			return DeviceScope.ANALYTICS;
		case "https://www.googleapis.com/auth/analytics.readonly":
			return DeviceScope.ANALYTICS_READ_ONLY;
		default:
			return DeviceScope.NONE;
		}
	}
	
}

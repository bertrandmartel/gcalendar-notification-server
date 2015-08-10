package fr.bmartel.protocol.google.api.profile;

/**
 * User profile object
 * 
 * @author Bertrand Martel
 *
 */
public class UserProfile {

	private String gender = "";

	private String displayName = "";

	private String familyName = "";

	private String givenName = "";

	private String language = "";

	/**
	 * Initialize user profile
	 * 
	 * @param gender
	 * @param email
	 * @param displayName
	 * @param familyName
	 * @param givenName
	 * @param imageUrl
	 */
	public UserProfile(String gender, String displayName, String familyName, String givenName, String language) {
		this.gender = gender;
		this.displayName = displayName;
		this.familyName = familyName;
		this.givenName = givenName;
		this.language = language;
	}

	public String getGender() {
		return gender;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getGivenName() {
		return givenName;
	}

	public String getLanguage() {
		return language;
	}
}

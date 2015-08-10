package fr.bmartel.protocol.google.oauth.device;

/**
 * Oauth token object
 * 
 * @author Bertrand Martel
 *
 */
public class OauthToken {

	/**
	 * type of token (Bearer)
	 */
	private String tokenType = "";

	/**
	 * access token itself
	 */
	private String accessToken = "";

	/**
	 * token life time
	 */
	private int expiresIn = 0;

	/**
	 * Build token
	 * 
	 * @param accessToken
	 * @param tokenType
	 * @param expireIn
	 */
	public OauthToken(String accessToken, String tokenType, int expireIn) {
		this.tokenType = tokenType;
		this.accessToken = accessToken;
		this.expiresIn = expireIn;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}
}

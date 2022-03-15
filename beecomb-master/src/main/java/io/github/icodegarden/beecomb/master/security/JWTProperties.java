package io.github.icodegarden.beecomb.master.security;

import lombok.Getter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
public class JWTProperties {

	private String issuer;
	private String secretKey;
	private int tokenExpireSeconds;

	public JWTProperties(String issuer, String secretKey, int tokenExpireSeconds) {
		this.issuer = issuer;
		this.secretKey = secretKey;
		this.tokenExpireSeconds = tokenExpireSeconds;
	}

}
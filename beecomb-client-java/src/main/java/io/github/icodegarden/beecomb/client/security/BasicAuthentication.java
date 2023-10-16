package io.github.icodegarden.beecomb.client.security;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import io.github.icodegarden.nutrient.lang.tuple.Tuple2;
import io.github.icodegarden.nutrient.lang.tuple.Tuples;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class BasicAuthentication implements Authentication {

	private final String username;
	private final String password;

	public BasicAuthentication(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public Tuple2<String, String> httpToken() throws AuthenticationException {
		String str = username + ":" + password;
		try {
			byte[] encode = Base64.getEncoder().encode(str.getBytes("utf-8"));
			return Tuples.of("Authorization", "Basic " + new String(encode, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("ex on encode", e);
		}
	}

}

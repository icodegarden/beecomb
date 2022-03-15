package io.github.icodegarden.beecomb.master.security;

import java.util.Date;

import org.springframework.security.core.Authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JWTCreator {

	private JWTProperties jwtProperties;

	public JWTCreator(JWTProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	public String createJWT(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal == null) {
			throw new IllegalArgumentException("Authentication must not be null");
		}
		if (!(principal instanceof UserDetails)) {
			throw new IllegalArgumentException(
					"principal must instanceof UserDetails, giving type:" + principal.getClass());
		}
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		return createJWT(userDetails);
	}

	public String createJWT(UserDetails userDetails) {
		UserPO user = userDetails.getUser();

//		String authorities = userDetails.getAuthorities().stream().map(authoritie->{
//			return authoritie.getAuthority();
//		}).collect(Collectors.joining(","));

		long now = new Date().getTime();
		Date expiresAt = new Date(now + jwtProperties.getTokenExpireSeconds() * 1000);
		return JWT.create()
				.withSubject("auth")
				.withIssuer(jwtProperties.getIssuer())
				.withClaim("id", user.getId())
				.withClaim("username", user.getUsername())
				.withClaim("platformRole", user.getPlatformRole().name())
				.withExpiresAt(expiresAt)
				.sign(Algorithm.HMAC256(jwtProperties.getSecretKey()));
	}

}

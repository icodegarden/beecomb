package io.github.icodegarden.beecomb.master.security;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import io.github.icodegarden.nursery.springboot.security.SpringAuthentication;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class JWTCreatorTests extends AuthenticationBasedTests {

	@Test
	void createJWT() throws Exception {
		SpringAuthentication authentication = (SpringAuthentication) SecurityUtils.getAuthentication();

		JWTProperties jwtProperties = new JWTProperties("gddc", "gddc@jwt20220818", 3600 * 24 * 365);
		JWTCreator jwtCreator = new JWTCreator(jwtProperties);
		String createJWT = jwtCreator.createJWT(authentication);
		System.out.println(createJWT);
	}
}

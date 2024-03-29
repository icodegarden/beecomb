package io.github.icodegarden.beecomb.master.security;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.PlatformRole;
import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import io.github.icodegarden.nursery.springboot.security.SpringAuthentication;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AuthenticationBasedTests {

	Authentication authentication = new Authentication() {
		private static final long serialVersionUID = 1L;

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

		}

		@Override
		public boolean isAuthenticated() {
			return false;
		}

		@Override
		public Object getPrincipal() {
			UserPO userPO = new UserPO();
			userPO.setId(1L);
			userPO.setUsername("xff");
			userPO.setPassword("password");
			userPO.setPlatformRole(PlatformRole.Admin);
			return new UserDetails(userPO, Collections.emptyList());
		}

		@Override
		public Object getDetails() {
			return null;
		}

		@Override
		public Object getCredentials() {
			return null;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return null;
		}
	};
	
	@BeforeEach
	void initAuth() {
		SecurityUtils.setAuthentication(new SpringAuthentication(authentication));
	}
	
}

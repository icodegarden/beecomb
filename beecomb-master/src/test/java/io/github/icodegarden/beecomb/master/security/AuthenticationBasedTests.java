package io.github.icodegarden.beecomb.master.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AuthenticationBasedTests {

	static {
		SecurityUtils.setAuthentication(new Authentication() {
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
				userPO.setUsername("xff");
				userPO.setPassword("password");
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
		});
	}
}

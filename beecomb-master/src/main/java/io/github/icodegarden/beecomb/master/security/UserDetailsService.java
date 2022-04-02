package io.github.icodegarden.beecomb.master.security;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.query.UserQuery;
import io.github.icodegarden.beecomb.master.service.UserService;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Component
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

	@Autowired
	private UserService userService;

	@Override
	public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) {
		UserPO user = userService.findByUsername(username, UserQuery.With.WITH_LEAST);
		if (user == null) {
			throw new UsernameNotFoundException("User:" + username + " not found");
		}
		if (!user.getActived()) {
			throw new DisabledException("User:" + username + " was not activated");
		}

		// platform role
		GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(
				user.getPlatformRole() != null ? user.getPlatformRole().name() : "");
		List<GrantedAuthority> grantedAuthorities = new LinkedList<GrantedAuthority>();
		grantedAuthorities.add(grantedAuthority);

		return new UserDetails(user, grantedAuthorities);
	}
}

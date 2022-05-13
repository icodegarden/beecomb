package io.github.icodegarden.beecomb.master.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.commons.springboot.security.SpringUser;

/**
 * @author Fangfang.Xu
 */
public class UserDetails extends SpringUser {

	private static final long serialVersionUID = 1L;

	private UserPO user;

	public UserDetails(UserPO user, Collection<? extends GrantedAuthority> authorities) {
		super(user.getId().toString(), user.getUsername(), user.getPassword(), authorities);
		this.user = user;
	}

	public UserPO getUser() {
		return user;
	}

}

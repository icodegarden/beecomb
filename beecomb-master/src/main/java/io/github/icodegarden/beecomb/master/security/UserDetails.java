package io.github.icodegarden.beecomb.master.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;

/**
 * @author Fangfang.Xu
 */
public class UserDetails extends User {

	private static final long serialVersionUID = 1L;

	private UserPO user;

	public UserDetails(UserPO user, Collection<? extends GrantedAuthority> authorities) {
		super(user.getUsername(), user.getPassword(), authorities);
		this.user = user;
	}

	public UserPO getUser() {
		return user;
	}

}

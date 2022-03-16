package io.github.icodegarden.beecomb.master.security;

import io.github.icodegarden.commons.springboot.security.SecurityUtils;

/**
 * 
 * @author Fangfang.Xu
 */
public abstract class UserUtils {

	/**
	 * 
	 * @return Nullable
	 */
	public static Long getUserId() {
		UserDetails userDetails = (UserDetails) SecurityUtils.getAuthenticatedUser();
		if (userDetails != null) {
			return userDetails.getUser().getId();
		}
		return null;
	}

}

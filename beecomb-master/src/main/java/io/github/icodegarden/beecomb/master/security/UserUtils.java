package io.github.icodegarden.beecomb.master.security;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.nursery.springboot.security.SecurityUtils;

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

	/**
	 * 
	 * @return Nullable
	 */
	public static UserPO.PlatformRole getUserPlatformRole() {
		UserDetails userDetails = (UserDetails) SecurityUtils.getAuthenticatedUser();
		if (userDetails != null) {
			return userDetails.getUser().getPlatformRole();
		}
		return null;
	}
}

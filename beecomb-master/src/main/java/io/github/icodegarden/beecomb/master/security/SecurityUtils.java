package io.github.icodegarden.beecomb.master.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 
 * @author Fangfang.Xu
 */
public class SecurityUtils {

//	private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);
	private SecurityUtils() {}

	/**
	 * 
	 * @return Nullable
	 */
	public static Long getUserId() {
		UserDetails userDetails = getAuthenticatedUser();
		if (userDetails != null) {
			return userDetails.getUser().getId();
		}
		return null;
	}
	
	/**
	 * 
	 * @return Nullable
	 */
	public static String getUsername() {
		UserDetails userDetails = getAuthenticatedUser();
		if (userDetails != null) {
			return userDetails.getUsername();
		}
		return null;
	}
	
	public static void setAuthentication(Authentication authentication) {
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
	
	/**
	 * 
	 * @return Nullable
	 */
	public static Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	/**
	 * 
	 * @return Nullable
	 */
	public static UserDetails getAuthenticatedUser() {
		Authentication authentication = getAuthentication();
		if (authentication != null) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof UserDetails) {
				return (UserDetails) principal;
			}
		}
		return null;
	}
}

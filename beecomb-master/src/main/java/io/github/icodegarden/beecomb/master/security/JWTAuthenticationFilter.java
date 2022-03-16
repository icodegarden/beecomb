package io.github.icodegarden.beecomb.master.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

/**
 * @author Fangfang.Xu
 */
public class JWTAuthenticationFilter extends GenericFilterBean {

	private JWTProperties jwtProperties;

	private boolean cookieEnable;

	private RequestMatcher pathMatcher;

	public JWTAuthenticationFilter(JWTProperties jwtProperties, List<String> matchPaths) {
		this.jwtProperties = jwtProperties;

		List<RequestMatcher> list = matchPaths.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList());
		pathMatcher = new OrRequestMatcher(list);
	}

	public JWTAuthenticationFilter setCookieEnable(boolean cookieEnable) {
		this.cookieEnable = cookieEnable;
		return this;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		/**
		 * 只对匹配的path进行认证
		 */
		if (!matchPath(request, response)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String jwt = WebUtils.getJWT();

			if (cookieEnable && !StringUtils.hasText(jwt)) {
				jwt = getJwtFromCookie(request);
			}

			if (StringUtils.hasText(jwt)) {
				try {
					JWTResolver jwtResolver = new JWTResolver(jwtProperties, jwt);
					Authentication authentication = jwtResolver.getAuthentication();
					SecurityUtils.setAuthentication(authentication);

					/**
					 * only create and resposne newjwt request by outer<br>
					 * the objectives of create newjwt ware: <br>
					 * 1 prevent session timeout suddenly<br>
					 * 2 prevent jwt just expired after rpc called<br>
					 * 
					 * @author Fangfang.Xu
					 */
					if (!WebUtils.isInternalRpc()
							&& jwtResolver.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(15))) {
						JWTCreator jwtCreator = new JWTCreator(jwtProperties);
						jwt = jwtCreator.createJWT(authentication);
						WebUtils.responseJWT(jwt, response);
					}

//					WebUtils.setJWT(jwt);
				} catch (TokenExpiredException e) {
					WebUtils.responseWrite(401, null, "Access Denied, Unauthorized, Token Expired", response);
					return;
				} catch (JWTVerificationException e) {
					WebUtils.responseWrite(401, null, "Access Denied, Unauthorized, Verification Error", response);
					return;
				}
			} else if (WebUtils.isInternalRpc()) {
//				SecurityUtils.setAuthentication(systemAuthentication);
			}

			filterChain.doFilter(request, response);
		} finally {
			SecurityUtils.setAuthentication(null);
		}
	}

	private String getJwtFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (WebUtils.AUTHORIZATION_HEADER.equals(cookie.getName())) {
				String value = cookie.getValue();
				return WebUtils.resolveBearerToken(value, "_");
			}
		}
		return null;
	}

	private boolean matchPath(HttpServletRequest request, HttpServletResponse response) {
		return pathMatcher.matches(request);
	}
}

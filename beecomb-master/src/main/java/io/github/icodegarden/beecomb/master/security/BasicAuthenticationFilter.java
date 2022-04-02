package io.github.icodegarden.beecomb.master.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.query.UserQuery;
import io.github.icodegarden.beecomb.master.service.UserService;
import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

/**
 * @author Fangfang.Xu
 */
public class BasicAuthenticationFilter extends GenericFilterBean {

	private final Map<String, Tuple2<UserPO, LocalDateTime>> cachedUsers = new HashMap<String, Tuple2<UserPO, LocalDateTime>>();

	private final UserService userService;
	private final Config config;
	private RequestMatcher pathMatcher;

	public static class Config {
		private final List<String> matchPaths;
		private final int maxUserCacheSeconds;

		public Config(List<String> matchPaths, int maxUserCacheSeconds) {
			super();
			this.matchPaths = matchPaths;
			this.maxUserCacheSeconds = maxUserCacheSeconds;
		}

		public List<String> getMatchPaths() {
			return matchPaths;
		}

		public int getMaxUserCacheSeconds() {
			return maxUserCacheSeconds;
		}
	}

	public BasicAuthenticationFilter(UserService userService, Config config) {
		this.userService = userService;
		this.config = config;

		List<RequestMatcher> list = config.getMatchPaths().stream().map(AntPathRequestMatcher::new)
				.collect(Collectors.toList());
//		AntPathRequestMatcher[] ants = { new AntPathRequestMatcher("/openapi/**") };
		pathMatcher = new OrRequestMatcher(list);
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
			String token = WebUtils.getBasicAuthorizationToken();
			if (StringUtils.hasText(token)) {
				String username = null;
				String password = null;

				try {
					Tuple2<String, String> tuple2 = decodeUsernamePassword(token);
					username = tuple2.getT1();
					password = tuple2.getT2();
				} catch (Exception e) {
					/**
					 * 解码失败401
					 */
					WebUtils.responseWrite(401, null, "Access Denied, Unauthorized, Bad Token", response);
					return;
				}

				/**
				 * TODO 使用cache框架来处理自动过期
				 */
				/**
				 * 校验用户名密码
				 * 
				 */
				Tuple2<UserPO, LocalDateTime> tuple = cachedUsers.compute(username, (k, v) -> {
					if (v == null
							|| v.getT2().isBefore(SystemUtils.now().minusSeconds(config.getMaxUserCacheSeconds()))) {
						UserPO user = userService.findByUsername(k, UserQuery.With.WITH_LEAST);
						if (user == null) {
							/**
							 * 返回null时map会自动清空该kv
							 */
							return null;
						}
						/**
						 * 有值返回时map会自动保持 kv
						 */
						return Tuples.of(user, SystemUtils.now());
					}
					return v;
				});

				UserPO user = null;
				if (tuple != null) {
					user = tuple.getT1();
				}

				if (user == null) {
					WebUtils.responseWrite(401, null, "Access Denied, Bad Credentials", response);
					return;
				}
				if (!user.getActived()) {
					WebUtils.responseWrite(401, null, "Access Denied, Not Activated", response);
					return;
				}
				boolean matches = userService.matchesPassword(password, user.getPassword());
				if (!matches) {
					WebUtils.responseWrite(401, null, "Access Denied, Bad Credentials", response);
					return;
				}

				/**
				 * ok
				 */
				Authentication authentication = buildAuthentication(user.getId(), user.getUsername());
				SecurityUtils.setAuthentication(authentication);
			}

			filterChain.doFilter(request, response);
		} finally {
			SecurityUtils.setAuthentication(null);
		}
	}

	private boolean matchPath(HttpServletRequest request, HttpServletResponse response) {
		return pathMatcher.matches(request);
	}

	private Authentication buildAuthentication(Long userId, String username) {
		Collection<GrantedAuthority> authoritys = Collections.emptyList();

		UserPO user = new UserPO();
		user.setId(userId);
		user.setUsername(username);
		user.setPassword("");

		UserDetails userDetails = new UserDetails(user, authoritys);
		return new PreAuthenticatedAuthenticationToken(userDetails, "", authoritys);
	}

	private Tuple2<String, String> decodeUsernamePassword(String token) throws Exception {
		Decoder decoder = Base64.getDecoder();
		byte[] bs = decoder.decode(token);
		String usernamePw = new String(bs, "utf-8");
		String[] split = usernamePw.split(":");
		String username = split[0];
		String password = split[1];

		if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
			throw new IllegalArgumentException("basic token must contains username and password");
		}
		return Tuples.of(username, password);
	}
}

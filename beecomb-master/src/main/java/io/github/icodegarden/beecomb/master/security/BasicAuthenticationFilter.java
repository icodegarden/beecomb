package io.github.icodegarden.beecomb.master.security;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.github.icodegarden.beecomb.master.manager.UserManager;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.query.UserQuery;
import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import io.github.icodegarden.nursery.springboot.security.SpringAuthentication;
import io.github.icodegarden.nursery.springboot.web.reactive.security.ReactiveNativeRestApiAuthenticationEntryPoint;
import io.github.icodegarden.nursery.springboot.web.reactive.util.ReactiveWebUtils;
import io.github.icodegarden.nutrient.lang.tuple.Tuple2;
import io.github.icodegarden.nutrient.lang.tuple.Tuples;
import io.github.icodegarden.nutrient.lang.util.ReactiveUtils;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;
import reactor.core.publisher.Mono;

/**
 * @author Fangfang.Xu
 */
public class BasicAuthenticationFilter implements WebFilter {

	private final Map<String, Tuple2<UserPO, LocalDateTime>> cachedUsers = new HashMap<String, Tuple2<UserPO, LocalDateTime>>();

	private final UserManager userService;
	private final Config config;

	private final ServerWebExchangeMatcher authMatcher;

	private final AuthenticationWebFilter authenticationWebFilter;

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

	public BasicAuthenticationFilter(UserManager userService, Config config) {
		this.userService = userService;
		this.config = config;

		List<ServerWebExchangeMatcher> matchers = config.getMatchPaths().stream()
				.map(PathPatternParserServerWebExchangeMatcher::new).collect(Collectors.toList());
		authMatcher = new OrServerWebExchangeMatcher(matchers);

		authenticationWebFilter = new AuthenticationWebFilter(new NoOpReactiveAuthenticationManager());

		authenticationWebFilter.setServerAuthenticationConverter(new InnerServerAuthenticationConverter());

		authenticationWebFilter.setAuthenticationSuccessHandler(new InnerServerAuthenticationSuccessHandler());

		/**
		 * 需要设置，默认使用的是HttpBasicServerAuthenticationEntryPoint
		 */
		authenticationWebFilter.setAuthenticationFailureHandler(new InnerServerAuthenticationFailureHandler());
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		if (ReactiveUtils.block(authMatcher.matches(exchange)).isMatch()) {
			return authenticationWebFilter.filter(exchange, chain);
		} else {
			return chain.filter(exchange);
		}
	}

	private class NoOpReactiveAuthenticationManager implements ReactiveAuthenticationManager {

		@Override
		public Mono<Authentication> authenticate(Authentication authentication) {
			/**
			 * 不校验，能生成就代表通过
			 */
			return Mono.just(authentication);
		}
	}

	private class InnerServerAuthenticationConverter implements ServerAuthenticationConverter {
		@Override
		public Mono<Authentication> convert(ServerWebExchange exchange) {
			try {
				/**
				 * 此时ReactiveWebUtils还没有exchange，需要先设置一下才能使用
				 */
				ReactiveWebUtils.setExchange(exchange);
				
				String token = ReactiveWebUtils.getBasicAuthorizationToken();
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
						ReactiveWebUtils.responseWrite(401, null, "Access Denied, Unauthorized, Bad Token", exchange);
						return null;
					}

					/**
					 * 校验用户名密码
					 * 
					 */
					Tuple2<UserPO, LocalDateTime> tuple = cachedUsers.compute(username, (k, v) -> {
						if (v == null || v.getT2()
								.isBefore(SystemUtils.now().minusSeconds(config.getMaxUserCacheSeconds()))) {
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
						return ReactiveWebUtils.responseWrite(401, null, "Access Denied, Bad Credentials", exchange);
					}
					if (!user.getActived()) {
						return ReactiveWebUtils.responseWrite(401, null, "Access Denied, Not Activated", exchange);
					}
					boolean matches = userService.matchesPassword(password, user.getPassword());
					if (!matches) {
						return ReactiveWebUtils.responseWrite(401, null, "Access Denied, Bad Credentials", exchange);
					}

					/**
					 * ok
					 */
					Authentication authentication = buildAuthentication(user.getId(), user.getUsername());
					return Mono.just(authentication);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				ReactiveWebUtils.setExchange(null);
			}

			return Mono.empty();
		}
	}

	private class InnerServerAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
		@Override
		public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
			WebFilterChain chain = webFilterExchange.getChain();
			ServerWebExchange exchange = webFilterExchange.getExchange();
			
			SpringAuthentication springAuthentication = new SpringAuthentication(authentication);

			exchange.getAttributes().put("authentication", springAuthentication);

			SecurityUtils.setAuthentication(springAuthentication);
			
			return chain.filter(exchange).doFinally(s -> SecurityUtils.setAuthentication(null));
		}
	}

	private class InnerServerAuthenticationFailureHandler implements ServerAuthenticationFailureHandler {

		private final ServerAuthenticationEntryPoint authenticationEntryPoint;

		public InnerServerAuthenticationFailureHandler() {
			this.authenticationEntryPoint = new ReactiveNativeRestApiAuthenticationEntryPoint();
		}

		@Override
		public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange,
				AuthenticationException exception) {
			return authenticationEntryPoint.commence(webFilterExchange.getExchange(), exception);
		}
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

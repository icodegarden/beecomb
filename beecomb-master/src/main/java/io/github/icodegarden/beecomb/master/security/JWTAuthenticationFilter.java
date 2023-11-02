package io.github.icodegarden.beecomb.master.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpCookie;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import io.github.icodegarden.nursery.springboot.security.SpringAuthentication;
import io.github.icodegarden.nursery.springboot.security.User;
import io.github.icodegarden.nursery.springboot.web.reactive.security.ReactiveNativeRestApiAuthenticationEntryPoint;
import io.github.icodegarden.nursery.springboot.web.reactive.util.ReactiveWebUtils;
import io.github.icodegarden.nursery.springboot.web.util.WebUtils;
import io.github.icodegarden.nutrient.lang.util.ReactiveUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author Fangfang.Xu
 */
public class JWTAuthenticationFilter implements WebFilter {

	private JWTProperties jwtProperties;

	private boolean cookieEnable;

	private final ServerWebExchangeMatcher authMatcher;

	private final AuthenticationWebFilter authenticationWebFilter;

	public JWTAuthenticationFilter(JWTProperties jwtProperties, List<String> matchPaths) {
		this.jwtProperties = jwtProperties;

		List<ServerWebExchangeMatcher> matchers = matchPaths.stream()
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

				String jwt = ReactiveWebUtils.getJWT();

				if (cookieEnable && !StringUtils.hasText(jwt)) {
					jwt = getJwtFromCookie(exchange);
				}

				if (StringUtils.hasText(jwt)) {
					try {
						JWTResolver jwtResolver = new JWTResolver(jwtProperties, jwt);
						Authentication authentication = jwtResolver.getAuthentication();
						return Mono.just(authentication);
					} catch (TokenExpiredException e) {
						/**
						 * 过期
						 */
						return ReactiveWebUtils.responseWrite(401, null, "Not Authenticated, Token Expired", exchange);
					} catch (JWTDecodeException | SignatureVerificationException e) {
						/**
						 * jwt token不合法
						 */
						return ReactiveWebUtils.responseWrite(401, null, "Not Authenticated, Token Invalid", exchange);
					} catch (JWTVerificationException e) {
						/**
						 * 算法或字段设置有问题
						 */
						return ReactiveWebUtils.responseWrite(500, null, "Verification Token Error", exchange);
					}
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

	public JWTAuthenticationFilter setCookieEnable(boolean cookieEnable) {
		this.cookieEnable = cookieEnable;
		return this;
	}

	private String getJwtFromCookie(ServerWebExchange exchange) {
		MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();

		List<HttpCookie> list = cookies.get(WebUtils.HEADER_AUTHORIZATION);
		if (list != null && !list.isEmpty()) {
			HttpCookie cookie = list.get(0);
			String value = cookie.getValue();
			return WebUtils.resolveBearerToken(value, "_");
		}
		return null;
	}

}

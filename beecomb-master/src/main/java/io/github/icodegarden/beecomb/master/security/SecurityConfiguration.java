package io.github.icodegarden.beecomb.master.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerAdapter;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import io.github.icodegarden.beecomb.master.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.master.configuration.InstanceProperties.Security.Jwt;
import io.github.icodegarden.beecomb.master.manager.UserManager;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.nursery.springboot.web.reactive.security.ReactiveNativeRestApiAccessDeniedHandler;
import io.github.icodegarden.nursery.springboot.web.reactive.security.ReactiveNativeRestApiAuthenticationEntryPoint;

/**
 * @author Fangfang.Xu
 */
@Configuration
@EnableWebFluxSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration {

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private UserManager userService;
	@Autowired
	private InstanceProperties instanceProperties;

	@Bean
	public ReactiveAuthenticationManager reactiveAuthenticationManager(PasswordEncoder passwordEncoder)
			throws Exception {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setPasswordEncoder(passwordEncoder);
		authenticationProvider.setUserDetailsService(userDetailsService);

//		DefaultAuthenticationEventPublisher eventPublisher = objectPostProcessor
//				.postProcess(new DefaultAuthenticationEventPublisher());
//		AuthenticationManagerBuilder auth = new AuthenticationManagerBuilder(objectPostProcessor);
//		auth.authenticationEventPublisher(eventPublisher);
//		auth.authenticationProvider(authenticationProvider);
//		return auth.build();

		return new ReactiveAuthenticationManagerAdapter(new ProviderManager(authenticationProvider));
	}

//	@Autowired
//	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
//	}

//    @Override
//    public void configure(WebSecurity web) throws Exception {
//    	IgnoredRequestConfigurer antMatchers = web.ignoring()
//        .antMatchers(HttpMethod.OPTIONS, "/**");
////    	for (String s : //白名单) {
////    		antMatchers = antMatchers.antMatchers(s);
////        }
//    }

	@Bean
	public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
		Jwt jwtConfig = instanceProperties.getSecurity().getJwt();
		JWTProperties jwtProperties = new JWTProperties(jwtConfig.getIssuer(), jwtConfig.getSecretKey(),
				jwtConfig.getTokenExpireSeconds());

		return http//
//				.sessionManagement()//
//				.sessionCreationPolicy(SessionCreationPolicy.NEVER)//
		// .maximumSessions(32) // maximum number of concurrent sessions for one user
		// .sessionRegistry(sessionRegistry)
//				.and()//
				.exceptionHandling()//
				.authenticationEntryPoint(new ReactiveNativeRestApiAuthenticationEntryPoint())//
				.accessDeniedHandler(new ReactiveNativeRestApiAccessDeniedHandler())//
				.and()//
				.csrf()//
				.disable()//
				.headers()//
				.frameOptions()//
				.disable()//
				.and()//
				.authorizeExchange()
//          	.pathMatchers("/swagger-ui/index.html").permitAll()//
				.pathMatchers("/openapi/*/version").permitAll()//
				.pathMatchers("/view/**").authenticated()//
				.pathMatchers("/api/user/**").hasAuthority(UserPO.PlatformRole.Admin.name())// 用户管理模块只对管理员开放
				.pathMatchers("/api/**").authenticated()//
				.pathMatchers("/openapi/**").authenticated()//
				.anyExchange().permitAll()//
				.and()//
				.addFilterBefore(new JWTAuthenticationFilter(jwtProperties,
						Arrays.asList("/api/**", "/view/**", "/system/main"/* main页面需要用户信息 */)).setCookieEnable(true),
						SecurityWebFiltersOrder.AUTHENTICATION)//
				.addFilterBefore(
						new BasicAuthenticationFilter(userService,
								new BasicAuthenticationFilter.Config(Arrays.asList("/openapi/**"),
										instanceProperties.getSecurity().getBasicAuth().getMaxUserCacheSeconds())),
						SecurityWebFiltersOrder.AUTHENTICATION)//
				.build();
	}
}

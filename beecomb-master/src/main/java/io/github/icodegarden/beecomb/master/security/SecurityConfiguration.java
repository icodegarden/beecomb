package io.github.icodegarden.beecomb.master.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.github.icodegarden.beecomb.master.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.master.configuration.InstanceProperties.Security.Jwt;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.service.UserService;
import io.github.icodegarden.commons.springboot.security.AccessDeniedHandler;
import io.github.icodegarden.commons.springboot.security.Http401UnauthorizedEntryPoint;

/**
 * @author Fangfang.Xu
 */
@ConditionalOnProperty(value = "ros.security.default.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
	private UserService userService;
    @Autowired
	private InstanceProperties instanceProperties;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

//    @Override
//    public void configure(WebSecurity web) throws Exception {
//    	IgnoredRequestConfigurer antMatchers = web.ignoring()
//        .antMatchers(HttpMethod.OPTIONS, "/**");
////    	for (String s : //白名单) {
////    		antMatchers = antMatchers.antMatchers(s);
////        }
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
		Jwt jwtConfig = instanceProperties.getSecurity().getJwt();
		JWTProperties jwtProperties = new JWTProperties(jwtConfig.getIssuer(), jwtConfig.getSecretKey(),
				jwtConfig.getTokenExpireSeconds());
    	
        http
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.NEVER)
            //.maximumSessions(32) // maximum number of concurrent sessions for one user
            //.sessionRegistry(sessionRegistry)
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(new Http401UnauthorizedEntryPoint())
            .accessDeniedHandler(new AccessDeniedHandler())
        .and()
            .csrf()
            .disable()
            .headers()
            .frameOptions()
            .disable()
        .and()
            .authorizeRequests()
//          .antMatchers("/swagger-ui/index.html").permitAll()
            .antMatchers("/view/**").authenticated()
            .antMatchers("/api/**/users/**").hasAuthority(UserPO.PlatformRole.Admin.name())//用户管理模块只对管理员开放
            .antMatchers("/api/**/user/**").hasAuthority(UserPO.PlatformRole.Admin.name())//用户管理模块只对管理员开放
            .antMatchers("/api/**").authenticated()
            .antMatchers("/openapi/**").authenticated()
        .and()
        	.addFilterBefore(new JWTAuthenticationFilter(jwtProperties, Arrays.asList("/api/**","/view/**")).setCookieEnable(true),
        			UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(
						new BasicAuthenticationFilter(userService,
								new BasicAuthenticationFilter.Config(Arrays.asList("/openapi/**"),
										instanceProperties.getSecurity().getBasicAuth().getMaxUserCacheSeconds())),
						UsernamePasswordAuthenticationFilter.class);
    }
}

package io.github.icodegarden.beecomb.master.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import io.github.icodegarden.beecomb.master.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.master.configuration.InstanceProperties.Security.Jwt;
import io.github.icodegarden.beecomb.master.pojo.view.UserVO;
import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import io.github.icodegarden.nursery.springboot.security.SpringAuthentication;
import io.github.icodegarden.nursery.springboot.web.reactive.util.ReactiveWebUtils;
import io.github.icodegarden.nutrient.lang.exception.reactive.ReactiveBlockException;
import io.github.icodegarden.nutrient.lang.util.ReactiveUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Fangfang.Xu
 */
@Slf4j
@Validated
@RestController
public class AuthenticationController {

//	private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

	@Autowired
	private InstanceProperties instanceProperties;
	@Autowired
	private ReactiveAuthenticationManager authenticationManager;

	@PostMapping(value = "authenticate")
	public ResponseEntity<UserVO> authenticate(@Validated @RequestBody LoginDTO loginDTO, ServerWebExchange exchange)
			throws Throwable {
		try {
			String username = loginDTO.getUsername();
			String pwd = loginDTO.getPassword();
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
					pwd);
			Authentication authentication = ReactiveUtils
					.block(this.authenticationManager.authenticate(authenticationToken), 1000L);
			SecurityUtils.setAuthentication(new SpringAuthentication(authentication));

			Jwt jwtConfig = instanceProperties.getSecurity().getJwt();

			JWTCreator jwtCreator = new JWTCreator(new JWTProperties(jwtConfig.getIssuer(), jwtConfig.getSecretKey(),
					jwtConfig.getTokenExpireSeconds()));
			String jwt = jwtCreator.createJWT(authentication);

			ReactiveWebUtils.responseJWT(jwt, exchange);

			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			return ResponseEntity.ok(new UserVO(userDetails.getUser()));
		} catch (AuthenticationException e) {
			if (e instanceof AuthenticationServiceException) {
				log.error("ex on authenticate, username:{}", loginDTO.getUsername(), e);
				throw e;
			}
			return (ResponseEntity) ResponseEntity.status(401).body(e.getMessage());
		} catch (ReactiveBlockException t) {
			if (!(t.getCause() instanceof AuthenticationException)) {
				throw t;
			}
			AuthenticationException e = (AuthenticationException) t.getCause();

			if (e.getCause() != null && !(e.getCause() instanceof AuthenticationServiceException)) {
				return (ResponseEntity) ResponseEntity.status(401).body(e.getMessage());
			}

			if (e instanceof AuthenticationServiceException) {
				log.error("ex on authenticate, username:{}", loginDTO.getUsername(), e);
				throw e;
			}
			return (ResponseEntity) ResponseEntity.status(401).body(e.getMessage());
		}
	}

	@Data
	public static class LoginDTO {
		@NotEmpty
		private String username;
		@NotEmpty
		private String password;
	}
}

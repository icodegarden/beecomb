package io.github.icodegarden.beecomb.master.security;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.beecomb.master.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.master.configuration.InstanceProperties.Security.Jwt;
import io.github.icodegarden.beecomb.master.pojo.view.UserVO;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.security.SpringAuthentication;
import io.github.icodegarden.commons.springboot.web.util.ServletWebUtils;
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
	private AuthenticationManager authenticationManager;

	@PostMapping(value = "authenticate")
	public ResponseEntity<UserVO> authenticate(@Validated @RequestBody LoginDTO loginDTO,
			HttpServletResponse response) {
		try {
			String username = loginDTO.getUsername();
			String pwd = loginDTO.getPassword();
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
					pwd);
			Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
			SecurityUtils.setAuthentication(new SpringAuthentication(authentication));

			Jwt jwtConfig = instanceProperties.getSecurity().getJwt();

			JWTCreator jwtCreator = new JWTCreator(new JWTProperties(jwtConfig.getIssuer(), jwtConfig.getSecretKey(),
					jwtConfig.getTokenExpireSeconds()));
			String jwt = jwtCreator.createJWT(authentication);

			ServletWebUtils.responseJWT(jwt, response);

			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			return ResponseEntity.ok(new UserVO(userDetails.getUser()));
		} catch (AuthenticationException e) {
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

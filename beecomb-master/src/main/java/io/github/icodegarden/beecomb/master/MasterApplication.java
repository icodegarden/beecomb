package io.github.icodegarden.beecomb.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;
import io.github.icodegarden.commons.springboot.SpringContext;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication
public class MasterApplication {

	public static void main(String[] args) {
		SpringApplication.run(MasterApplication.class, args);

		Environment env = SpringContext.getApplicationContext().getBean(Environment.class);
		String applicationName = env.getRequiredProperty("spring.application.name");
		ServerErrorCodeException.configApplicationName(applicationName);
	}

}
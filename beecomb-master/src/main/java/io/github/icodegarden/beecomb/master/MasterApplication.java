package io.github.icodegarden.beecomb.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;
import io.github.icodegarden.commons.springboot.SpringContext;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = { "io.github.icodegarden.beecomb.master",
		"io.github.icodegarden.beecomb.common.backend"})
public class MasterApplication {

	public static void main(String[] args) {// TODO 优化索引
		SpringApplication.run(MasterApplication.class, args);

		Environment env = SpringContext.getApplicationContext().getBean(Environment.class);
		String applicationName = env.getRequiredProperty("spring.application.name");
		ServerErrorCodeException.configApplicationName(applicationName);
	}

}
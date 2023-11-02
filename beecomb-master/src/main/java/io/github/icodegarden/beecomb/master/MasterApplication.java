package io.github.icodegarden.beecomb.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.icodegarden.nursery.springboot.web.reactive.util.ReactiveWebUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication(scanBasePackages = { "io.github.icodegarden.beecomb.master",
		"io.github.icodegarden.beecomb.common.backend" })
public class MasterApplication {

	public static void main(String[] args) {
		ReactiveWebUtils.initGeneralServerConfig();
		
		SpringApplication.run(MasterApplication.class, args);
	}

}
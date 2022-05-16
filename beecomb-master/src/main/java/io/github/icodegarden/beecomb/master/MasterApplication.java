package io.github.icodegarden.beecomb.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = { "io.github.icodegarden.beecomb.master",
		"io.github.icodegarden.beecomb.common.backend" })
public class MasterApplication {

	public static void main(String[] args) {
		SpringApplication.run(MasterApplication.class, args);
	}

}
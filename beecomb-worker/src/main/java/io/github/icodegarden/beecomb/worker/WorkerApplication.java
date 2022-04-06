package io.github.icodegarden.beecomb.worker;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication(scanBasePackages = { "io.github.icodegarden.beecomb.worker",
		"io.github.icodegarden.beecomb.common.backend.manager" })
public class WorkerApplication {

	public static void main(String[] args) throws IOException, InterruptedException {
		SpringApplication.run(WorkerApplication.class, args);

		String applicationName = InstanceProperties.singleton().getApplicationName();
		ServerErrorCodeException.configApplicationName(applicationName);
	}
}
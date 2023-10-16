package io.github.icodegarden.beecomb.worker;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication(scanBasePackages = { "io.github.icodegarden.beecomb.worker",
		"io.github.icodegarden.beecomb.common.backend" })
public class WorkerApplication {

	public static void main(String[] args) throws IOException, InterruptedException {
		SpringApplication.run(WorkerApplication.class, args);
	}
}
package io.github.icodegarden.beecomb.worker;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import io.github.icodegarden.nutrient.lang.metricsregistry.InstanceRegistry;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication(scanBasePackages = { "io.github.icodegarden.beecomb.worker",
		"io.github.icodegarden.beecomb.common.backend" })
public class WorkerApplication {

	public static void main(String[] args) throws IOException, InterruptedException {
		ConfigurableApplicationContext ac = SpringApplication.run(WorkerApplication.class, args);
		
		InstanceRegistry registry = ac.getBean(InstanceRegistry.class);
		/*
		 *  要在服务启动之后再注册，否则可能出现短暂的master连不上worker、worker没准备好处理失败等
		 */
		registry.registerIfNot();
	}
}
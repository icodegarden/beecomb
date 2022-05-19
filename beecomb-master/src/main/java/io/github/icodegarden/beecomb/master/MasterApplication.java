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
		//TODO 页面创建任务
		//TODO 手动进入队列按钮（需要枷锁控制并发）		
		SpringApplication.run(MasterApplication.class, args);
	}

}
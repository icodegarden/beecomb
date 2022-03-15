package io.github.icodegarden.beecomb.master.configuration.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceRegistry;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
public class TestBeansConfiguration {

	@Bean("workerRegister")
	public ZooKeeperInstanceRegistry workerRegister(ZooKeeperHolder zooKeeperHolder) {
		return new ZooKeeperInstanceRegistry(zooKeeperHolder, "/beecomb", "worker", 9999);
	}
}

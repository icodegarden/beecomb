package io.github.icodegarden.beecomb.executor.registry.zookeeper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.beecomb.executor.registry.zookeeper.ZooKeeperJobHandlerRegistry;
import io.github.icodegarden.beecomb.test.ZookeeperBuilder4Test;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceRegistry;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ZooKeeperJobHandlerRegistryTests extends ZookeeperBuilder4Test {

	ZooKeeperInstanceRegistry zooKeeperInstanceRegistry;
	ZooKeeperJobHandlerRegistry zooKeeperJobHandlerRegistry;
	MyJobHandler h1;
	MyJobHandler h2;

	@BeforeEach
	void init() {
		zooKeeperInstanceRegistry = new ZooKeeperInstanceRegistry(zkh, "/beecomb", "executor", 10001);
		zooKeeperJobHandlerRegistry = new ZooKeeperJobHandlerRegistry("myExecutorName", zkh, zooKeeperInstanceRegistry);

		h1 = new MyJobHandler("h1");
		h2 = new MyJobHandler("h2");

		List<JobHandler> jobHandlers = Arrays.asList(h1, h2);
		zooKeeperJobHandlerRegistry.registerReplace(jobHandlers);
	}

	@Test
	void registerAppend() throws Exception {
		MyJobHandler h3 = new MyJobHandler("h3");
		MyJobHandler h4 = new MyJobHandler("h4");
		zooKeeperJobHandlerRegistry.registerAppend(Arrays.asList(h3, h4));

		Collection<? extends JobHandler> jobHandlers = zooKeeperJobHandlerRegistry.listJobHandlers();
		assertThat(jobHandlers).hasSize(4);
		//再次h3
		Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> zooKeeperJobHandlerRegistry.registerAppend(Arrays.asList(h3)));
	}

	@Test
	void registerReplace() throws Exception {
		MyJobHandler h3 = new MyJobHandler("h3");
		MyJobHandler h4 = new MyJobHandler("h4");
		//2个h4
		Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> zooKeeperJobHandlerRegistry.registerAppend(Arrays.asList(h3, h4, h4)));
	}

	@Test
	void getJobHandler() throws Exception {
		JobHandler jobHandler = zooKeeperJobHandlerRegistry.getJobHandler("h2");
		assertThat(jobHandler).isNotNull();
		assertThat(jobHandler).isEqualTo(h2);
	}

	@Test
	void listJobHandlers() throws Exception {
		Collection<? extends JobHandler> listJobHandlers = zooKeeperJobHandlerRegistry.listJobHandlers();

		List<JobHandler> jobHandlers = Arrays.asList(h1, h2);
		assertThat(jobHandlers).containsAll(listJobHandlers);
	}

	@Test
	void deregister() throws Exception {
		List<JobHandler> jobHandlers = Arrays.asList(h2);
		zooKeeperJobHandlerRegistry.deregister(jobHandlers);

		Collection<? extends JobHandler> listJobHandlers = zooKeeperJobHandlerRegistry.listJobHandlers();
		assertThat(listJobHandlers).hasSize(1);
		assertThat(listJobHandlers.iterator().next()).isEqualTo(h1);
	}

	@Test
	void deregisterAll() throws Exception {
		zooKeeperJobHandlerRegistry.deregisterAll();

		Collection<? extends JobHandler> listJobHandlers = zooKeeperJobHandlerRegistry.listJobHandlers();
		assertThat(listJobHandlers).isEmpty();
		;
	}

	private class MyJobHandler implements JobHandler {
		private String name;

		public MyJobHandler(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public ExecuteJobResult handle(Job job) {
			return null;
		}
	}

}

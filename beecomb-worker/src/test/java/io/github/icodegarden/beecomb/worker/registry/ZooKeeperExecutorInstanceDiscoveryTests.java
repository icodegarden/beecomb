package io.github.icodegarden.beecomb.worker.registry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean.JobHandlerRegistration;
import io.github.icodegarden.beecomb.test.ZookeeperBuilder4Test;
import io.github.icodegarden.beecomb.worker.registry.zookeeper.ZooKeeperExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.worker.registry.zookeeper.ZooKeeperExecutorRegisteredInstance;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceRegistry;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ZooKeeperExecutorInstanceDiscoveryTests extends ZookeeperBuilder4Test {

	@Test
	void listInstances() throws Exception {
		ZooKeeperExecutorInstanceDiscovery instanceDiscovery = new ZooKeeperExecutorInstanceDiscovery(zkh, "/beecomb");

		// 还未注册，没有实例-------------------------
		List<ZooKeeperExecutorRegisteredInstance> workers = instanceDiscovery.listInstances("executor");
		assertThat(workers).isEmpty();

		// 注册后，实例1，但没有JobHandlerRegistration信息-------------------------
		ZooKeeperInstanceRegistry instanceRegistry = new ZooKeeperInstanceRegistry(zkh, "/beecomb", "executor", 10000);
		instanceRegistry.registerIfNot();

		workers = instanceDiscovery.listInstances("executor");
		assertThat(workers).hasSize(1);
		assertThat(workers.get(0)).isInstanceOf(ZooKeeperExecutorRegisteredInstance.class);
		assertThat(((ZooKeeperExecutorRegisteredInstance) workers.get(0)).getJobHandlerRegistrationBean()).isNull();

		// 设置JobHandlerRegistration信息后-------------------------
		JobHandlerRegistrationBean jobHandlerRegistrationBean = new JobHandlerRegistrationBean();
		jobHandlerRegistrationBean.setExecutorName("myExecutor");
		JobHandlerRegistration jobHandlerRegistration1 = new JobHandlerRegistration();
		jobHandlerRegistration1.setJobHandlerName("job1");
		JobHandlerRegistration jobHandlerRegistration2 = new JobHandlerRegistration();
		jobHandlerRegistration2.setJobHandlerName("job2");
		jobHandlerRegistrationBean.setJobHandlerRegistrations(
				new HashSet<JobHandlerRegistration>(Arrays.asList(jobHandlerRegistration1, jobHandlerRegistration2)));

		instanceRegistry.setData(JsonUtils.serialize(jobHandlerRegistrationBean).getBytes("utf-8"));

		workers = instanceDiscovery.listInstances("executor");
		assertThat(workers).hasSize(1);
		assertThat(workers.get(0)).isInstanceOf(ZooKeeperExecutorRegisteredInstance.class);
		assertThat((workers.get(0)).getJobHandlerRegistrationBean()).isNotNull();
		assertThat((workers.get(0)).getJobHandlerRegistrationBean().getExecutorName()).isEqualTo("myExecutor");
		assertThat((workers.get(0)).getJobHandlerRegistrationBean().getJobHandlerRegistrations()).hasSize(2);
		assertThat((workers.get(0)).getJobHandlerRegistrationBean().getJobHandlerRegistrations())
				.contains(jobHandlerRegistration1).contains(jobHandlerRegistration2);
	}

	
}

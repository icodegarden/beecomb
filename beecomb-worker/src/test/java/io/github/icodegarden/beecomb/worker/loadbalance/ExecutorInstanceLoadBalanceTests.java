package io.github.icodegarden.beecomb.worker.loadbalance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean.JobHandlerRegistration;
import io.github.icodegarden.beecomb.worker.loadbalance.ExecutorInstanceLoadBalance;
import io.github.icodegarden.beecomb.worker.registry.ExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.worker.registry.ExecutorRegisteredInstance;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.Metrics.DimensionName;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ExecutorInstanceLoadBalanceTests {

	final String executorName = "myExecutor";
	final String jobHandlerName = "myjob";

	final ExecutorInstanceDiscovery<ExecutorRegisteredInstance> executorInstanceDiscovery = mock(
			ExecutorInstanceDiscovery.class);
	final InstanceMetrics<Metrics> instanceMetrics = mock(InstanceMetrics.class);
	final ExecutorInstanceLoadBalance executorLoadBalance = new ExecutorInstanceLoadBalance(executorInstanceDiscovery,
			instanceMetrics, executorName, jobHandlerName);

	@BeforeEach
	void init() {
		doReturn(Collections.emptyList()).when(instanceMetrics).listMetrics("executor");
	}

	@Test
	void selectCandidates_return0_onDiscovery0() throws Exception {
		// mock实例发现0，结果0-------------------------
		doReturn(Collections.emptyList()).when(executorInstanceDiscovery).listInstances(any());

		Queue<MetricsInstance> candidates = executorLoadBalance.selectCandidates(null, 3);
		assertThat(candidates).isEmpty();
	}

	@Test
	void selectCandidates_return0_onDiscovery1serviceNameNotEq() throws Exception {
		// mock实例发现1，但serviceName不符合，结果0-------------------------
		ExecutorRegisteredInstance executorRegisteredInstance = new ExecutorRegisteredInstance.Default("noteq",
				"executor1", "1.1.1.1", 10000, null);
		List<ExecutorRegisteredInstance> instances = Arrays.asList(executorRegisteredInstance);
		doReturn(instances).when(executorInstanceDiscovery).listInstances(any());

		Queue<MetricsInstance> candidates = executorLoadBalance.selectCandidates(null, 3);
		assertThat(candidates).isEmpty();
	}

	@Test
	void selectCandidates_return0_onDiscovery1executorNameNotEq() throws Exception {
		// mock实例发现1，serviceName也符合，但executorName不符合，结果0-------------------------
		JobHandlerRegistrationBean jobHandlerRegistrationBean = new JobHandlerRegistrationBean();
		jobHandlerRegistrationBean.setExecutorName("noteq");
		ExecutorRegisteredInstance executorRegisteredInstance = new ExecutorRegisteredInstance.Default(
				NodeRole.Executor.getRoleName(), "executor1", "1.1.1.1", 10000, jobHandlerRegistrationBean);
		List<ExecutorRegisteredInstance> instances = Arrays.asList(executorRegisteredInstance);
		doReturn(instances).when(executorInstanceDiscovery).listInstances(any());

		Queue<MetricsInstance> candidates = executorLoadBalance.selectCandidates(null, 3);
		assertThat(candidates).isEmpty();
	}

	@Test
	void selectCandidates_return0_onDiscovery1jobHandlerNameNotEq() throws Exception {
		// mock实例发现1，serviceName也符合，executorName也符合，但jobHandlerName不符合，结果0-------------------------
		JobHandlerRegistrationBean jobHandlerRegistrationBean = new JobHandlerRegistrationBean();
		jobHandlerRegistrationBean.setExecutorName(executorName);

		JobHandlerRegistration jobHandlerRegistration1 = new JobHandlerRegistration();
		jobHandlerRegistration1.setJobHandlerName("noteq1");
		JobHandlerRegistration jobHandlerRegistration2 = new JobHandlerRegistration();
		jobHandlerRegistration2.setJobHandlerName("noteq2");
		jobHandlerRegistrationBean.setJobHandlerRegistrations(
				new HashSet<JobHandlerRegistration>(Arrays.asList(jobHandlerRegistration1, jobHandlerRegistration2)));

		ExecutorRegisteredInstance executorRegisteredInstance = new ExecutorRegisteredInstance.Default(
				NodeRole.Executor.getRoleName(), "executor1", "1.1.1.1", 10000, jobHandlerRegistrationBean);

		List<ExecutorRegisteredInstance> instances = Arrays.asList(executorRegisteredInstance);
		doReturn(instances).when(executorInstanceDiscovery).listInstances(any());

		Queue<MetricsInstance> candidates = executorLoadBalance.selectCandidates(null, 3);
		assertThat(candidates).isEmpty();
	}

	@Test
	void selectCandidates_return2_onDiscovery2AllEq() throws Exception {
		// mock实例发现1，serviceName符合，executorName符合，jobHandlerName符合，结果1-------------------------
		JobHandlerRegistrationBean jobHandlerRegistrationBean = new JobHandlerRegistrationBean();
		jobHandlerRegistrationBean.setExecutorName(executorName);

		JobHandlerRegistration jobHandlerRegistration1 = new JobHandlerRegistration();
		jobHandlerRegistration1.setJobHandlerName("noteq1");
		JobHandlerRegistration jobHandlerRegistration2 = new JobHandlerRegistration();
		jobHandlerRegistration2.setJobHandlerName(jobHandlerName);
		jobHandlerRegistrationBean.setJobHandlerRegistrations(
				new HashSet<JobHandlerRegistration>(Arrays.asList(jobHandlerRegistration1, jobHandlerRegistration2)));

		/**
		 * lb需要
		 */
		Metrics metrics = new Metrics(new Metrics.Dimension(new DimensionName("ignore"), 1, 0));
		metrics.setServiceName(NodeRole.Executor.getRoleName());
		doReturn(Arrays.asList(metrics)).when(instanceMetrics).listMetrics(any());
		/**
		 * lb需要
		 */
		ExecutorRegisteredInstance executorRegisteredInstance1 = new ExecutorRegisteredInstance.Default(
				NodeRole.Executor.getRoleName(), "executor1", "1.1.1.1", 10000, jobHandlerRegistrationBean);
		ExecutorRegisteredInstance executorRegisteredInstance2 = new ExecutorRegisteredInstance.Default(
				NodeRole.Executor.getRoleName(), "executor2", "1.1.1.2", 10000, jobHandlerRegistrationBean);
		List<ExecutorRegisteredInstance> instances = Arrays.asList(executorRegisteredInstance1,
				executorRegisteredInstance2);
		doReturn(instances).when(executorInstanceDiscovery).listInstances(any());

		Queue<MetricsInstance> candidates = executorLoadBalance.selectCandidates(null, 3);
		assertThat(candidates).hasSize(2);
	}
}

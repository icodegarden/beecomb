package io.github.icodegarden.beecomb.worker.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.common.backend.executor.registry.DefaultExecutorRegisteredInstance;
import io.github.icodegarden.beecomb.common.backend.executor.registry.ExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.common.backend.executor.registry.ExecutorRegisteredInstance;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean.JobHandlerRegistration;
import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.test.NioClientSuppliers4Test;
import io.github.icodegarden.beecomb.test.Properties4Test;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.worker.core.AbstractJobEngine.JobTrigger;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.beecomb.worker.service.DelayJobService;
import io.github.icodegarden.commons.exchange.nio.NioProtocol;
import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.MetricsOverload;
import io.github.icodegarden.commons.lang.result.Result3;
import io.github.icodegarden.commons.lang.result.Results;
import io.github.icodegarden.commons.nio.pool.NioClientPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class DelayJobEngineTests extends Properties4Test {

	InstanceProperties instanceProperties = new InstanceProperties();

	final static String EXECUTOR_NAME = "myExecutor";
	final static String JOB_HANDLER_NAME = "myHandler";

	ExecutorInstanceDiscovery<ExecutorRegisteredInstance> executorInstanceDiscovery;
	InstanceMetrics<Metrics> instanceMetrics;
	MetricsOverload metricsOverload;
	DelayJobService delayJobService;
	DelayJobEngine delayJobEngine;

	@BeforeEach
	void init() {
		executorInstanceDiscovery = mock(ExecutorInstanceDiscovery.class);
		instanceMetrics = mock(InstanceMetrics.class);
		metricsOverload = mock(MetricsOverload.class);
		delayJobService = mock(DelayJobService.class);

		delayJobEngine = new DelayJobEngine(executorInstanceDiscovery, instanceMetrics, metricsOverload, delayJobService,
				instanceProperties);
		assertThat(delayJobEngine.queuedSize()).isEqualTo(0);
	}

	ExecutableJobBO getJob() {
		ExecutableJobBO job = new ExecutableJobBO();
		job.setId(1L);
		job.setType(JobType.Delay);
		job.setExecutorName(EXECUTOR_NAME);
		job.setJobHandlerName(JOB_HANDLER_NAME);
		job.setExecuteTimeout(3000);
		job.setCreatedAt(LocalDateTime.now());
		job.setUpdatedAt(LocalDateTime.now());
		job.setPriority(5);
		job.setWeight(1);
		job.setParallel(new Random().nextBoolean());
		job.setMaxParallelShards(8);
		job.setLastExecuteSuccess(true);
		job.setEnd(false);
		DelayBO delay = new DelayBO();
		delay.setDelay(1000L);
		delay.setRetryBackoffOnNoQualified(2000);
		delay.setRetryBackoffOnExecuteFailed(2000);
		delay.setRetriedTimesOnExecuteFailed(0);
		delay.setRetriedTimesOnNoQualified(0);
		delay.setRetryOnExecuteFailed(0);
		delay.setRetryOnNoQualified(0);
		job.setDelay(delay);
		return job;
	}

	@Test
	void runJob_noQualifiedExecutor_noThreshold() {
		// --------------------------------???????????????executor???CASE ??????????????????reEnQueue
		doReturn(Collections.emptyList()).when(executorInstanceDiscovery).listInstances(anyString());// ???????????????executor
		doReturn(Results.of(true, false/* ???????????? */, null)).when(delayJobService).updateOnNoQualifiedExecutor(any());

		ExecutableJobBO job = getJob();
		delayJobEngine.runJob(job);

		verify(delayJobService, times(1)).updateOnNoQualifiedExecutor(any());// ??????????????????
		verify(metricsOverload, times(0)).decrementOverload(job);// 0???
		verify(metricsOverload, times(0)).flushMetrics();// 0???
		assertThat(delayJobEngine.queuedSize()).isEqualTo(1);// reEnQueue???????????????1???
	}

	@Test
	void runJob_noQualifiedExecutor_threshold() {
		// --------------------------------???????????????executor???CASE ???????????????????????? decrementOverload
		// and flushMetrics
		doReturn(Collections.emptyList()).when(executorInstanceDiscovery).listInstances(anyString());// ???????????????executor
		doReturn(Results.of(true, true/* ????????? */, null)).when(delayJobService).updateOnNoQualifiedExecutor(any());

		ExecutableJobBO job = getJob();
		delayJobEngine.runJob(job);

		verify(delayJobService, times(1)).updateOnNoQualifiedExecutor(any());// ??????????????????
		verify(metricsOverload, times(1)).decrementOverload(job);// ????????????
		verify(metricsOverload, times(1)).flushMetrics();// ??????

		assertThat(delayJobEngine.queuedSize()).isEqualTo(0);// ?????????0
	}

	/**
	 * ??????????????????
	 */
	@Test
	void runJob_ok() {
		DelayJobEngine.protocol_for_Test = new NioProtocol(NioClientPool.newPool("new",
				NioClientSuppliers4Test.returnExchangeResultAlwaysSuccess(new ExecuteJobResult())));
		DelayJobEngine delayJobEngine = new DelayJobEngine(executorInstanceDiscovery, instanceMetrics, metricsOverload,
				delayJobService, instanceProperties);

		JobHandlerRegistrationBean jobHandlerRegistrationBean = new JobHandlerRegistrationBean();
		jobHandlerRegistrationBean.setExecutorName(EXECUTOR_NAME);
		JobHandlerRegistration jobHandlerRegistration1 = new JobHandlerRegistration();
		jobHandlerRegistration1.setJobHandlerName(JOB_HANDLER_NAME);
		jobHandlerRegistrationBean.setJobHandlerRegistrations(
				new HashSet<JobHandlerRegistration>(Arrays.asList(jobHandlerRegistration1)));
		
		ExecutorRegisteredInstance executorRegisteredInstance = new DefaultExecutorRegisteredInstance(
				NodeRole.Executor.getRoleName(), "instance1", "1.1.1.1", 10001, jobHandlerRegistrationBean);
		doReturn(Arrays.asList(executorRegisteredInstance)).when(executorInstanceDiscovery).listInstances(anyString());
		
		doReturn(Results.of(true, null)).when(delayJobService).updateOnExecuteSuccess(any());

		ExecutableJobBO job = getJob();
		delayJobEngine.runJob(job);

		verify(delayJobService, times(1)).updateOnExecuteSuccess(any());// ??????????????????
		verify(metricsOverload, times(1)).decrementOverload(job);// ????????????
		verify(metricsOverload, times(1)).flushMetrics();// ??????

		assertThat(delayJobEngine.queuedSize()).isEqualTo(0);// ?????????0

		DelayJobEngine.protocol_for_Test = null;
	}

	/**
	 * ???????????????exchange??????
	 */
	@Test
	void runJob_nok_noThreshold() {
		DelayJobEngine.protocol_for_Test = new NioProtocol(
				NioClientPool.newPool("new", NioClientSuppliers4Test.returnExchangeResultAlwaysFailed()));
		DelayJobEngine delayJobEngine = new DelayJobEngine(executorInstanceDiscovery, instanceMetrics, metricsOverload,
				delayJobService, instanceProperties);

		JobHandlerRegistrationBean jobHandlerRegistrationBean = new JobHandlerRegistrationBean();
		jobHandlerRegistrationBean.setExecutorName(EXECUTOR_NAME);
		JobHandlerRegistration jobHandlerRegistration1 = new JobHandlerRegistration();
		jobHandlerRegistration1.setJobHandlerName(JOB_HANDLER_NAME);
		jobHandlerRegistrationBean.setJobHandlerRegistrations(
				new HashSet<JobHandlerRegistration>(Arrays.asList(jobHandlerRegistration1)));
		
		/**
		 * lb??????
		 */
		ExecutorRegisteredInstance executorRegisteredInstance = new DefaultExecutorRegisteredInstance(
				NodeRole.Executor.getRoleName(), "instance1", "1.1.1.1", 10001, jobHandlerRegistrationBean);
		doReturn(Arrays.asList(executorRegisteredInstance)).when(executorInstanceDiscovery).listInstances(anyString());
		/**
		 * ???????????????
		 */
		doReturn(Results.of(true, false/* ???????????? */, null)).when(delayJobService).updateOnExecuteFailed(any());

		ExecutableJobBO job = getJob();
		delayJobEngine.runJob(job);

		verify(delayJobService, times(1)).updateOnExecuteFailed(any());// ??????????????????
		verify(metricsOverload, times(0)).decrementOverload(job);// 0???
		verify(metricsOverload, times(0)).flushMetrics();// 0???

		assertThat(delayJobEngine.queuedSize()).isEqualTo(1);// reEnQueue???????????????1???

		DelayJobEngine.protocol_for_Test = null;
	}

	/**
	 * ????????????exchange??????
	 */
	@Test
	void runJob_nok_threshold() {
		DelayJobEngine.protocol_for_Test = new NioProtocol(
				NioClientPool.newPool("new", NioClientSuppliers4Test.returnExchangeResultAlwaysFailed()));
		DelayJobEngine delayJobEngine = new DelayJobEngine(executorInstanceDiscovery, instanceMetrics, metricsOverload,
				delayJobService, instanceProperties);

		// --------------------------------CASE ???????????????????????? decrementOverload and
		// flushMetrics
		JobHandlerRegistrationBean jobHandlerRegistrationBean = new JobHandlerRegistrationBean();
		jobHandlerRegistrationBean.setExecutorName(EXECUTOR_NAME);
		JobHandlerRegistration jobHandlerRegistration1 = new JobHandlerRegistration();
		jobHandlerRegistration1.setJobHandlerName(JOB_HANDLER_NAME);
		jobHandlerRegistrationBean.setJobHandlerRegistrations(
				new HashSet<JobHandlerRegistration>(Arrays.asList(jobHandlerRegistration1)));
		
		
		ExecutorRegisteredInstance executorRegisteredInstance = new DefaultExecutorRegisteredInstance(
				NodeRole.Executor.getRoleName(), "instance1", "1.1.1.1", 10001, jobHandlerRegistrationBean);
		doReturn(Arrays.asList(executorRegisteredInstance)).when(executorInstanceDiscovery).listInstances(anyString());
		
		doReturn(Results.of(true, true/* ????????? */, null)).when(delayJobService).updateOnExecuteFailed(any());

		ExecutableJobBO job = getJob();
		delayJobEngine.runJob(job);

		verify(delayJobService, times(1)).updateOnExecuteFailed(any());// ??????????????????
		verify(metricsOverload, times(1)).decrementOverload(job);// 0???
		verify(metricsOverload, times(1)).flushMetrics();// 0???

		assertThat(delayJobEngine.queuedSize()).isEqualTo(0);// 0???

		DelayJobEngine.protocol_for_Test = null;
	}

	@Test
	void doEnQueue() throws Exception {
		// ??????executorInstanceDiscovery??????????????????????????????
		ExecutableJobBO job = getJob();
		doReturn(job).when(delayJobService).findOneExecutableJob(anyLong());
		doReturn(true).when(metricsOverload).incrementOverload(job);
		
		Result3<ExecutableJobBO, JobTrigger, JobEngineException> result3 = delayJobEngine.enQueue(job);

		assertThat(result3.isSuccess()).isTrue();
		assertThat(delayJobEngine.queuedSize()).isEqualTo(1);
		verify(executorInstanceDiscovery, times(0)).listInstances(anyString());

		assertThat(delayJobEngine.queuedSize()).isEqualTo(1);//
		
		// ??????????????????????????????????????????
		JobTrigger jobTrigger = result3.getT2();
		while (jobTrigger.getExecutedTimes() != 1) {// ????????????????????????
			Thread.yield();
		}
		verify(executorInstanceDiscovery, times(1)).listInstances(anyString());
		
		assertThat(delayJobEngine.queuedSize()).isEqualTo(0);//
	}

	@Test
	void removeQueue() {
		ExecutableJobBO job = getJob();
		doReturn(job).when(delayJobService).findOneExecutableJob(anyLong());
		doReturn(true).when(metricsOverload).incrementOverload(job);
		
		Result3<ExecutableJobBO, JobTrigger, JobEngineException> result3 = delayJobEngine.enQueue(job);
		assertThat(delayJobEngine.queuedSize()).isEqualTo(1);

		
		boolean b = delayJobEngine.removeQueue(job);
		assertThat(b).isTrue();
		assertThat(delayJobEngine.queuedSize()).isEqualTo(0);
		
		b = delayJobEngine.removeQueue(job);
		assertThat(b).isTrue();//???????????????????????????true
	}
	
}

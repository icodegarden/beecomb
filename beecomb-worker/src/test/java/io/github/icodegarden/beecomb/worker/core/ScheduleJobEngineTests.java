package io.github.icodegarden.beecomb.worker.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
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
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ScheduleBO;
import io.github.icodegarden.beecomb.test.NioClientSuppliers4Test;
import io.github.icodegarden.beecomb.test.Properties4Test;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.worker.core.AbstractJobEngine.JobTrigger;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.beecomb.worker.service.ScheduleJobService;
import io.github.icodegarden.commons.exchange.nio.NioProtocol;
import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.MetricsOverload;
import io.github.icodegarden.commons.lang.result.Result3;
import io.github.icodegarden.commons.lang.result.Results;
import io.github.icodegarden.commons.nio.pool.NioClientPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ScheduleJobEngineTests extends Properties4Test {
	
	InstanceProperties instanceProperties = new InstanceProperties();
	
	final static String EXECUTOR_NAME = "myExecutor";
	final static String JOB_HANDLER_NAME = "myHandler";

	ExecutorInstanceDiscovery<ExecutorRegisteredInstance> executorInstanceDiscovery;
	InstanceMetrics instanceMetrics;
	MetricsOverload metricsOverload;
	ScheduleJobService scheduleJobService;
	ScheduleJobEngine scheduleJobEngine;

	@BeforeEach
	void init() {
		executorInstanceDiscovery = mock(ExecutorInstanceDiscovery.class);
		instanceMetrics = mock(InstanceMetrics.class);
		metricsOverload = mock(MetricsOverload.class);
		scheduleJobService = mock(ScheduleJobService.class);

		scheduleJobEngine = new ScheduleJobEngine(executorInstanceDiscovery, instanceMetrics, metricsOverload,
				scheduleJobService, instanceProperties);
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(0);
	}

	ExecutableJobBO getJob() {
		ExecutableJobBO job = new ExecutableJobBO();
		job.setId(1L);
		job.setType(JobType.Schedule);
		job.setExecutorName(EXECUTOR_NAME);
		job.setJobHandlerName(JOB_HANDLER_NAME);
		job.setExecuteTimeout(3000);
		job.setCreatedAt(LocalDateTime.now());
		job.setPriority(5);
		job.setWeight(1);
		job.setParallel(new Random().nextBoolean());
		job.setMaxParallelShards(8);
		job.setLastExecuteSuccess(true);
		job.setEnd(false);
		ScheduleBO schedule = new ScheduleBO();
		schedule.setScheduledTimes(0L);
		job.setSchedule(schedule);
		return job;
	}

	@Test
	void runJob_noQualifiedExecutor_scheduleFixDelay() throws Exception {
		// --------------------------------没有合格的executor
		ExecutableJobBO job = getJob();
		job.getSchedule().setScheduleFixDelay(1000L);
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_noQualifiedExecutor(job);
	}

	@Test
	void runJob_noQualifiedExecutor_scheduleFixRate() throws Exception {
		// --------------------------------没有合格的executor
		ExecutableJobBO job = getJob();
		job.getSchedule().setScheduleFixRate(1000L);
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_noQualifiedExecutor(job);
	}

	@Test
	void runJob_noQualifiedExecutor_cron() throws Exception {
		// --------------------------------没有合格的executor
		ExecutableJobBO job = getJob();
		job.getSchedule().setSheduleCron("* * * * * *");// 每秒
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_noQualifiedExecutor(job);
	}

	void runJob_noQualifiedExecutor(ExecutableJobBO job) throws Exception {
		// --------------------------------没有合格的executor
		doReturn(Collections.emptyList()).when(executorInstanceDiscovery).listInstances(anyString());// 没有合格的executor
		doReturn(Results.of(true, true/* 对阈值无感 */, null)).when(scheduleJobService).updateOnNoQualifiedExecutor(any());

		Result3<ExecutableJobBO, JobTrigger, JobEngineException> result3 = scheduleJobEngine.doEnQueue(job);

		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(1);// 队列1个

		JobTrigger jobTrigger = result3.getT2();
		while (!jobTrigger.isRunning()) {// 等待处于运行中
			Thread.yield();
		}
		assertThat(jobTrigger.getExecutedTimes()).isEqualTo(0);// 还未执行完成
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(0);// 任务执行中时，队列0
		while (jobTrigger.getExecutedTimes() != 1) {// 等待任务执行完毕
			Thread.yield();
		}

		verify(scheduleJobService, times(1)).updateOnNoQualifiedExecutor(any());// 触发数据更新
		verify(metricsOverload, times(0)).decrementOverload(job);// 0次
		verify(metricsOverload, times(0)).flushMetrics();// 0次
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(1);// 执行失败后队列依然1个
	}

	@Test
	void runJob_ok_scheduleFixDelay() throws Exception {
		ExecutableJobBO job = getJob();
		job.getSchedule().setScheduleFixDelay(1000L);
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_ok(job);
	}

	@Test
	void runJob_ok_scheduleFixRate() throws Exception {
		ExecutableJobBO job = getJob();
		job.getSchedule().setScheduleFixRate(1000L);
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_ok(job);
	}

	@Test
	void runJob_ok_cron() throws Exception {
		ExecutableJobBO job = getJob();
		job.getSchedule().setSheduleCron("* * * * * *");// 每秒
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_ok(job);
	}

	void runJob_ok(ExecutableJobBO job) throws Exception {
		ScheduleJobEngine.protocol_for_Test = new NioProtocol(NioClientPool.newPool("new",
				NioClientSuppliers4Test.returnExchangeResultAlwaysSuccess(new ExecuteJobResult())));
		ScheduleJobEngine scheduleJobEngine = new ScheduleJobEngine(executorInstanceDiscovery, instanceMetrics, metricsOverload,
				scheduleJobService, instanceProperties);
		
		JobHandlerRegistrationBean jobHandlerRegistrationBean = new JobHandlerRegistrationBean();
		jobHandlerRegistrationBean.setExecutorName(EXECUTOR_NAME);
		JobHandlerRegistration jobHandlerRegistration1 = new JobHandlerRegistration();
		jobHandlerRegistration1.setJobHandlerName(JOB_HANDLER_NAME);
		jobHandlerRegistrationBean.setJobHandlerRegistrations(
				new HashSet<JobHandlerRegistration>(Arrays.asList(jobHandlerRegistration1)));
		ExecutorRegisteredInstance executorRegisteredInstance = new DefaultExecutorRegisteredInstance(
				NodeRole.Executor.getRoleName(), "instance1", "1.1.1.1", 10001, jobHandlerRegistrationBean);

		doReturn(Arrays.asList(executorRegisteredInstance)).when(executorInstanceDiscovery).listInstances(anyString());
		doReturn(Results.of(true, null)).when(scheduleJobService).updateOnExecuteSuccess(any());

		Result3<ExecutableJobBO, JobTrigger, JobEngineException> result3 = scheduleJobEngine.doEnQueue(job);
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(1);// 队列1个

//		scheduleJobEngine.setNioClientPool(NioClientPool.newPool("new",
//				NioClientSuppliers4Test.returnExchangeResultAlwaysSuccess(new ExecuteJobResult())));

		JobTrigger jobTrigger = result3.getT2();
		while (!jobTrigger.isRunning()) {// 等待处于运行中
			Thread.yield();
		}
		assertThat(jobTrigger.getExecutedTimes()).isEqualTo(0);// 还未执行完成
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(0);// 任务执行中时，队列0
		while (jobTrigger.getExecutedTimes() != 1) {// 等待任务执行完毕
			Thread.yield();
		}

		verify(scheduleJobService, times(1)).updateOnExecuteSuccess(any());// 触发数据更新
		verify(metricsOverload, times(0)).decrementOverload(job);// 0次
		verify(metricsOverload, times(0)).flushMetrics();// 0次
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(1);// 成功后队列依然1个
		
		ScheduleJobEngine.protocol_for_Test = null;
	}

	@Test
	void runJob_ok_end_scheduleFixDelay() throws Exception {
		ExecutableJobBO job = getJob();
		job.getSchedule().setScheduleFixDelay(1000L);
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_ok_end(job);
	}

	@Test
	void runJob_ok_end_scheduleFixRate() throws Exception {
		ExecutableJobBO job = getJob();
		job.getSchedule().setScheduleFixRate(1000L);
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_ok_end(job);
	}

	@Test
	void runJob_ok_end_cron() throws Exception {
		ExecutableJobBO job = getJob();
		job.getSchedule().setSheduleCron("* * * * * *");// 每秒
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_ok_end(job);
	}

	void runJob_ok_end(ExecutableJobBO job) throws Exception {
		ExecuteJobResult executeJobResult = new ExecuteJobResult();
		executeJobResult.setEnd(true);// 要求end任务
		
		ScheduleJobEngine.protocol_for_Test = new NioProtocol(NioClientPool.newPool("new",
				NioClientSuppliers4Test.returnExchangeResultAlwaysSuccess(executeJobResult)));
		ScheduleJobEngine scheduleJobEngine = new ScheduleJobEngine(executorInstanceDiscovery, instanceMetrics, metricsOverload,
				scheduleJobService, instanceProperties);
		
		JobHandlerRegistrationBean jobHandlerRegistrationBean = new JobHandlerRegistrationBean();
		jobHandlerRegistrationBean.setExecutorName(EXECUTOR_NAME);
		JobHandlerRegistration jobHandlerRegistration1 = new JobHandlerRegistration();
		jobHandlerRegistration1.setJobHandlerName(JOB_HANDLER_NAME);
		jobHandlerRegistrationBean.setJobHandlerRegistrations(
				new HashSet<JobHandlerRegistration>(Arrays.asList(jobHandlerRegistration1)));
		ExecutorRegisteredInstance executorRegisteredInstance = new DefaultExecutorRegisteredInstance(
				NodeRole.Executor.getRoleName(), "instance1", "1.1.1.1", 10001, jobHandlerRegistrationBean);

		doReturn(Arrays.asList(executorRegisteredInstance)).when(executorInstanceDiscovery).listInstances(anyString());
		doReturn(Results.of(true, null)).when(scheduleJobService).updateOnExecuteSuccess(any());
		doReturn(true).when(metricsOverload).incrementOverload(job);

		Result3<ExecutableJobBO, JobTrigger, JobEngineException> result3 = scheduleJobEngine.enQueue(job);
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(1);// 队列1个

		JobTrigger jobTrigger = result3.getT2();
		while (!jobTrigger.isRunning()) {// 等待处于运行中
			Thread.yield();
		}
		assertThat(jobTrigger.getExecutedTimes()).isEqualTo(0);// 还未执行完成
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(0);// 任务执行中时，队列0
		while (jobTrigger.getExecutedTimes() != 1) {// 等待任务执行完毕
			Thread.yield();
		}

		verify(scheduleJobService, times(1)).updateOnExecuteSuccess(any());// 触发数据更新
		verify(metricsOverload, times(1)).decrementOverload(job);// 1次
		verify(metricsOverload, atLeastOnce()).flushMetrics();// 
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(0);// 成功后0个
		
		ScheduleJobEngine.protocol_for_Test = null;
	}

	@Test
	void runJob_nok_scheduleFixDelay() throws Exception {
		ExecutableJobBO job = getJob();
		job.getSchedule().setScheduleFixDelay(1000L);
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_nok(job);
	}

	@Test
	void runJob_nok_scheduleFixRate() throws Exception {
		ExecutableJobBO job = getJob();
		job.getSchedule().setScheduleFixRate(1000L);
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_nok(job);
	}

	@Test
	void runJob_nok_cron() throws Exception {
		ExecutableJobBO job = getJob();
		job.getSchedule().setSheduleCron("* * * * * *");// 每秒
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		
		runJob_nok(job);
	}

	void runJob_nok(ExecutableJobBO job) throws Exception {
		ScheduleJobEngine.protocol_for_Test = new NioProtocol(NioClientPool.newPool("new", NioClientSuppliers4Test.returnExchangeResultAlwaysFailed()));
		ScheduleJobEngine scheduleJobEngine = new ScheduleJobEngine(executorInstanceDiscovery, instanceMetrics, metricsOverload,
				scheduleJobService, instanceProperties);
		
		JobHandlerRegistrationBean jobHandlerRegistrationBean = new JobHandlerRegistrationBean();
		jobHandlerRegistrationBean.setExecutorName(EXECUTOR_NAME);
		JobHandlerRegistration jobHandlerRegistration1 = new JobHandlerRegistration();
		jobHandlerRegistration1.setJobHandlerName(JOB_HANDLER_NAME);
		jobHandlerRegistrationBean.setJobHandlerRegistrations(
				new HashSet<JobHandlerRegistration>(Arrays.asList(jobHandlerRegistration1)));
		ExecutorRegisteredInstance executorRegisteredInstance = new DefaultExecutorRegisteredInstance(
				NodeRole.Executor.getRoleName(), "instance1", "1.1.1.1", 10001, jobHandlerRegistrationBean);

		doReturn(Arrays.asList(executorRegisteredInstance)).when(executorInstanceDiscovery).listInstances(anyString());
		doReturn(Results.of(true, true/* 无感 */, null)).when(scheduleJobService).updateOnExecuteFailed(any());

		Result3<ExecutableJobBO, JobTrigger, JobEngineException> result3 = scheduleJobEngine.doEnQueue(job);
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(1);// 队列1个

		JobTrigger jobTrigger = result3.getT2();
		while (!jobTrigger.isRunning()) {// 等待处于运行中
			Thread.yield();
		}
		assertThat(jobTrigger.getExecutedTimes()).isEqualTo(0);// 还未执行完成
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(0);// 任务执行中时，队列0
		while (jobTrigger.getExecutedTimes() != 1) {// 等待任务执行完毕
			Thread.yield();
		}

		verify(scheduleJobService, times(1)).updateOnExecuteFailed(any());// 触发数据更新
		verify(metricsOverload, times(0)).decrementOverload(job);// 0次
		verify(metricsOverload, times(0)).flushMetrics();// 0次
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(1);// 成功后1个
		
		ScheduleJobEngine.protocol_for_Test = null;
	}

	@Test
	void removeQueue() {
		ExecutableJobBO job = getJob();
		job.getSchedule().setSheduleCron("* * * * * *");
		doReturn(job).when(scheduleJobService).findOneExecutableJob(anyLong());
		doReturn(true).when(metricsOverload).incrementOverload(job);
		
		Result3<ExecutableJobBO, JobTrigger, JobEngineException> result3 = scheduleJobEngine.enQueue(job);
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(1);

		
		boolean b = scheduleJobEngine.removeQueue(job);
		assertThat(b).isTrue();
		assertThat(scheduleJobEngine.queuedSize()).isEqualTo(0);
		
		b = scheduleJobEngine.removeQueue(job);
		assertThat(b).isTrue();//已不存在，返回也是true
	}
}

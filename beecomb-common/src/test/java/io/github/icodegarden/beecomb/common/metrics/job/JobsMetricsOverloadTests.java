package io.github.icodegarden.beecomb.common.metrics.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.metrics.job.JobsMetricsOverload.Config;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ScheduleBO;
import io.github.icodegarden.commons.lang.metricsregistry.DefaultRegisteredInstance;
import io.github.icodegarden.commons.lang.metricsregistry.InstanceMetrics;
import io.github.icodegarden.commons.lang.metricsregistry.InstanceRegistry;
import io.github.icodegarden.commons.lang.metricsregistry.Metrics;
import io.github.icodegarden.commons.lang.metricsregistry.Metrics.Dimension;
import io.github.icodegarden.commons.lang.metricsregistry.Metrics.DimensionName;
import io.github.icodegarden.commons.lang.metricsregistry.RegisteredInstance;
import io.github.icodegarden.commons.lang.tuple.NullableTuples;
import io.github.icodegarden.commons.lang.tuple.Tuples;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class JobsMetricsOverloadTests {

	static final int MAX_JOBS_OVERLOAD = 2;
	static {
		System.setProperty("overload.jobs.max", MAX_JOBS_OVERLOAD + "");// 关系到测试
	}
	
	JobsMetricsOverload allJobOverload;
	InstanceMetrics instanceMetrics;
	ExecutableJobBO job;

	@BeforeEach
	void init() {
		InstanceRegistry instanceRegistry = mock(InstanceRegistry.class);
		RegisteredInstance registeredInstance = new DefaultRegisteredInstance(NodeRole.Executor.getRoleName(),
				"executor1", "1.1.1.1", 10001);
		doReturn(registeredInstance).when(instanceRegistry).getRegistered();

		instanceMetrics = mock(InstanceMetrics.class);
		Metrics metrics = new Metrics(new Dimension(DimensionName.Cpu, 4, 0),
				new Dimension(DimensionName.Memory, 1024, 0), new Dimension(DimensionName.Jobs, 2, 0));
		metrics.setInstanceName(registeredInstance.getInstanceName());
		doReturn(metrics).when(instanceMetrics).getMetrics(registeredInstance);

		Config jobOverloadConfig = new JobsMetricsOverload.Config(NullableTuples.of(1.0, 1), NullableTuples.of(1024*1024.0, 1),
				Tuples.of(MAX_JOBS_OVERLOAD, 1));
		allJobOverload = new JobsMetricsOverload(instanceRegistry, instanceMetrics, jobOverloadConfig);

		job = new ExecutableJobBO();
		job.setType(JobType.Schedule);
		ScheduleBO schedule = new ScheduleBO();
//		schedule.setScheduleFixDelay(1000);
//		schedule.setScheduleFixRate(1000);
		schedule.setSheduleCron("* * * * * *");// 每秒
		job.setSchedule(schedule);
		job.setWeight(3);

		allJobOverload.resetOverload();
	}

	@Test
	void getMetrics() {
		Metrics metrics = allJobOverload.getMetrics();
		assertThat(metrics.getDimension(DimensionName.Cpu).getUsed()).isGreaterThanOrEqualTo(0);
		assertThat(metrics.getDimension(DimensionName.Memory).getUsed()).isGreaterThanOrEqualTo(0);
		assertThat(metrics.getDimension(DimensionName.Jobs).getUsed()).isEqualTo(0);
	}

	@Test
	void getLocalMetrics() {
		Metrics metrics = allJobOverload.getLocalMetrics();
		assertThat(metrics.getDimension(DimensionName.Cpu).getUsed()).isGreaterThanOrEqualTo(0);
		assertThat(metrics.getDimension(DimensionName.Memory).getUsed()).isGreaterThanOrEqualTo(0);
		assertThat(metrics.getDimension(DimensionName.Jobs).getUsed()).isEqualTo(0);
	}

	@Test
	void willOverload() {
		boolean result = allJobOverload.willOverload(job);
		assertThat(result).isTrue();

		// -------------------------------------------------
		job.setWeight(2);
		result = allJobOverload.willOverload(job);
		assertThat(result).isFalse();
	}

	@Test
	void incrementOverload() {
		getMetrics();// 使触发初始化

		// -----------------------------------
		boolean result = allJobOverload.incrementOverload(job);
		assertThat(result).isFalse();

		// -----------------------------------
		job.setWeight(2);
		result = allJobOverload.incrementOverload(job);
		assertThat(result).isTrue();

		// -----------------------------------
		Metrics metrics = allJobOverload.getLocalMetrics();
		assertThat(metrics.getDimension(DimensionName.Jobs).getUsed()).isEqualTo(2.0);
		metrics = allJobOverload.getMetrics();
		assertThat(metrics.getDimension(DimensionName.Jobs).getUsed()).isEqualTo(0);
	}

	@Test
	void decrementOverload() {
		job.setWeight(2);
		boolean result = allJobOverload.incrementOverload(job);
		assertThat(result).isTrue();

		allJobOverload.decrementOverload(job);

		Metrics metrics = allJobOverload.getLocalMetrics();
		assertThat(metrics.getDimension(DimensionName.Jobs).getUsed()).isEqualTo(0);
		metrics = allJobOverload.getMetrics();
		assertThat(metrics.getDimension(DimensionName.Jobs).getUsed()).isEqualTo(0);
	}

	@Test
	void flushMetrics() {
		job.setWeight(2);
		boolean result = allJobOverload.incrementOverload(job);
		assertThat(result).isTrue();

		clearInvocations(instanceMetrics);

		allJobOverload.flushMetrics();

		verify(instanceMetrics, times(1)).setMetrics(any(), any());

	}
}

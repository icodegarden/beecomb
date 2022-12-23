package io.github.icodegarden.beecomb.common.metrics.job;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.github.icodegarden.commons.lang.annotation.NotNull;
import io.github.icodegarden.commons.lang.metrics.DefaultMetricsOverload;
import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.Metrics.Dimension;
import io.github.icodegarden.commons.lang.metrics.Metrics.DimensionName;
import io.github.icodegarden.commons.lang.metrics.MetricsOverload;
import io.github.icodegarden.commons.lang.metrics.OverloadCalc;
import io.github.icodegarden.commons.lang.registry.InstanceRegistry;
import io.github.icodegarden.commons.lang.tuple.NullableTuple2;
import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JobsMetricsOverload implements MetricsOverload {
//	private static final Logger log = LoggerFactory.getLogger(JobsMetricsOverload.class);

	private Config config;

	private DefaultMetricsOverload defaultMetricsOverload;

	public static class Config {

		private final NullableTuple2<Double/*max*/, Integer/* 权重 */> cpu;
		private final NullableTuple2<Double/*max*/, Integer/* 权重 */> memory;
		private final Tuple2<Integer/* maxJobsOverload */, Integer/* 权重 */> jobs;

		public Config(NullableTuple2<Double, Integer> cpu, NullableTuple2<Double, Integer> memory,
				@NotNull Tuple2<Integer, Integer> jobs) {
			Objects.requireNonNull(jobs, "jobs must not null");
			this.cpu = cpu;
			this.memory = memory;
			this.jobs = jobs;
		}

		public NullableTuple2<Double, Integer> getCpu() {
			return cpu;
		}

		public NullableTuple2<Double, Integer> getMemory() {
			return memory;
		}

		public Tuple2<Integer, Integer> getJobs() {
			return jobs;
		}

	}

	public JobsMetricsOverload(InstanceRegistry instanceRegistry, InstanceMetrics instanceMetrics, Config config) {
		this.config = config;

		Metrics metrics = rebuildLocalMetrics();
		defaultMetricsOverload = new DefaultMetricsOverload(instanceRegistry, instanceMetrics, metrics);
		flushMetrics();
	}

	void resetOverload() {
		Metrics metrics = rebuildLocalMetrics();
		defaultMetricsOverload.resetMetrics(metrics);

		flushMetrics();
	}

	private Metrics rebuildLocalMetrics() {
		List<Dimension> dimensions = new LinkedList<Dimension>();
		/**
		 * cpu使用率，1.0表示100%
		 */
		double cpuMax = config.getCpu() != null ? config.getCpu().getT1() : 1.0;
		int cpuWeight = config.getCpu() != null ? config.getCpu().getT2() : 0;//权重0表示不使用
		Dimension cpuD = new Metrics.Dimension(DimensionName.Cpu, cpuMax, SystemUtils.getVmRuntime().getProcessCpuLoad(),
				cpuWeight);
		dimensions.add(cpuD);
		
		/**
		 * 单位MB
		 */
		double memoryMax = config.getMemory() != null ? config.getMemory().getT1() : SystemUtils.getVmRuntime().getJvmMaxMemory() / 1024 / 1024;
		int memoryWeight = config.getMemory() != null ? config.getMemory().getT2() : 0;//权重0表示不使用
		Dimension memoryD = new Metrics.Dimension(DimensionName.Memory, memoryMax,
				SystemUtils.getVmRuntime().getJvmUsedMemory() / 1024 / 1024, memoryWeight);
		dimensions.add(memoryD);
		
		/**
		 * jobs
		 */
		Tuple2<Integer, Integer> jobs = config.getJobs();
		Dimension jobsD = new Metrics.Dimension(DimensionName.Jobs, jobs.getT1(), 0, jobs.getT2());
		dimensions.add(jobsD);
		
		Map<String, Serializable> descMap = new HashMap<String, Serializable>();
		descMap.put("cpuCores", Runtime.getRuntime().availableProcessors());
		descMap.put("physicalMemory", SystemUtils.getVmRuntime().getTotalPhysicalMemorySize() / 1024 / 1024);
		
		Metrics metrics = new Metrics(dimensions);
		metrics.setDesc(JsonUtils.serialize(descMap));

		return metrics;
	}

	public void enableScheduleFlushMetrics(long scheduleMillis) {
		defaultMetricsOverload.enableScheduleFlushMetrics(scheduleMillis);
	}

	@Override
	public Metrics getMetrics() {
		return defaultMetricsOverload.getMetrics();
	}

	@Override
	public Metrics getLocalMetrics() {
		return defaultMetricsOverload.getLocalMetrics();
	}

	@Override
	public boolean willOverload(OverloadCalc obj) {
		return defaultMetricsOverload.willOverload(obj);
	}

	@Override
	public boolean incrementOverload(OverloadCalc obj) {
		return defaultMetricsOverload.incrementOverload(obj);
	}

	@Override
	public void decrementOverload(OverloadCalc obj) {
		defaultMetricsOverload.decrementOverload(obj);
	}

	@Override
	public void flushMetrics() {
		defaultMetricsOverload.flushMetrics();
	}

	@Override
	public void close() throws IOException {
		defaultMetricsOverload.close();
	}
}
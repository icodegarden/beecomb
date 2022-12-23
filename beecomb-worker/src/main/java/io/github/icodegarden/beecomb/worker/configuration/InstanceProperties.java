package io.github.icodegarden.beecomb.worker.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConfigurationProperties
@Setter
@Getter
@ToString
public class InstanceProperties {

	private static InstanceProperties singleton;

	public InstanceProperties() {
		singleton = this;
	}

	public static InstanceProperties singleton() {
		return singleton;
	}

	@Autowired
	private Environment env;

	private Server server = new Server();
	private Overload overload = new Overload();
	private ZooKeeper zookeeper = new ZooKeeper();
	private LoadBalance loadBalance = new LoadBalance();
	private Schedule schedule = new Schedule();

	public String getApplicationName() {
		return env.getRequiredProperty("spring.application.name");
	}

	@Setter
	@Getter
	@ToString
	public static class Server {
		private String bindIp = SystemUtils.getIp();
		private int port = 19898;
		/**
		 * 不健康的sql执行时间
		 */
		private long sqlUnhealthMillis = 100;
		/**
		 * 影响nioServer关闭时等待已接收处理中的任务完毕
		 */
		private long nioServerShutdownBlockingTimeoutMillis = 30000;
		/**
		 * 影响任务引擎shutdown时等待正在处理中的任务完毕
		 */
		private long engineShutdownBlockingTimeoutMillis = 60000;
	}

	@Setter
	@Getter
	@ToString
	public static class Overload {
		/**
		 * 默认memory权重0即不参与，因为jobs是通过cpu核memory综合计算得出的，使用OverloadCalc.ofOverload()控制memory的上限能力，例如大量任务的执行频率非常低时
		 */
		private Cpu cpu = new Cpu();
		private Memory memory = new Memory();
		private Jobs jobs = new Jobs();

		@Setter
		@Getter
		@ToString
		public static class Cpu {
			private double max = 0.9;// 不高于90%。系统最大1.0表示100%
			private int weight = 1;
		}

		@Setter
		@Getter
		@ToString
		public static class Memory {
			private double max = SystemUtils.getVmRuntime().getJvmMaxMemory() / 1024 / 1024;// MB
			private int weight = 0;
		}

		@Setter
		@Getter
		@ToString
		public static class Jobs {
			private int max = (int) SystemUtils.getVmRuntime().maxConcurrentThreadsPerSecond();
			private int weight = 1;
		}
	}

	@Setter
	@Getter
	@ToString
	public static class LoadBalance {
		private int maxCandidates = 3;
	}

	@Setter
	@Getter
	@ToString
	public static class Schedule {
		/**
		 * 刷新Executor的
		 */
		private int discoveryCacheRefreshIntervalMillis = 10000;
		/**
		 * 刷新Executor的
		 */
		private int metricsCacheRefreshIntervalMillis = 3000;
		/**
		 * 刷入自己的
		 */
		private int flushMetricsIntervalMillis = 3000;
	}
}

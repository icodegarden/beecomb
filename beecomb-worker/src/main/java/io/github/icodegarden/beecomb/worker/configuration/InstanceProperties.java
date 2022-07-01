package io.github.icodegarden.beecomb.worker.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.Data;
import lombok.Getter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
@Configuration
@ConfigurationProperties
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

	@Data
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
	
	@Data
	public static class Overload {
		/**
		 * 默认不需要开启cpu、memory，因为jobs是通过cpu核memory综合计算得出的，只使用jobs具有了cpu和memory的上限能力
		 */
		private Cpu cpu;
		private Memory memory;
		private Jobs jobs = new Jobs();

		@Data
		public static class Cpu {
			private int weight = 1;
		}

		@Data
		public static class Memory {
			private int weight = 1;
		}

		@Getter
		public static class Jobs {
			private int weight = 8;
			private int max = (int) SystemUtils.getVmRuntime().maxConcurrentThreadsPerSecond();

			public void setWeight(int weight) {
				this.weight = weight;
			}

			public void setMax(int max) {
				this.max = max;
			}
		}
	}

	@Data
	public static class LoadBalance {
		private int maxCandidates = 3;
	}

	@Data
	public static class Schedule {
		/**
		 * 
		 */
		private int discoveryCacheRefreshIntervalMillis = 10000;
		/**
		 * 
		 */
		private int metricsCacheRefreshIntervalMillis = 1000;
	}
}

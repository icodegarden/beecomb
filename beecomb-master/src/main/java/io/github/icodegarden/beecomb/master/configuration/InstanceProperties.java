package io.github.icodegarden.beecomb.master.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.icodegarden.nutrient.lang.util.SystemUtils;
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

	private Server server = new Server();
	private Job job = new Job();
	private ZooKeeper zookeeper = new ZooKeeper();
	private LoadBalance loadBalance = new LoadBalance();
	private Schedule schedule = new Schedule();
	private Security security = new Security();

	@Setter
	@Getter
	@ToString
	public static class Server {
		private String bindIp = SystemUtils.getIp();
		private int port;
	}

	@Setter
	@Getter
	@ToString
	public static class Job {
		/**
		 * dispatch的过程正常是很快的，但在服务刚启动使用阶段可能会需要更大的延迟（worker需要初始化数据库连接等）
		 */
		private int dispatchTimeoutMillis = 10000;
		
	}

	@Setter
	@Getter
	@ToString
	public static class ZooKeeper extends io.github.icodegarden.beecomb.common.properties.ZooKeeper {
		/**
		 * 因为lock不需要参与acl，而root有acl，所以跟root区分开
		 */
		private String lockRoot = "/beecomb-lock";
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
		 * 刷新Worker的
		 */
		private long discoveryCacheRefreshIntervalMillis = 10000;
		/**
		 * 刷新Worker的
		 */
		private long metricsCacheRefreshIntervalMillis = 3000;
		/**
		 * 刷入自己的
		 */
		private long flushMetricsIntervalMillis = 3000;
		
		private long recoveryScheduleMillis = 60000;
		/**
		 * 每天凌晨2点执行
		 */
		private String reportScheduleCron = "0 0 2 * * *";
		/**
		 * 每月15日02:00执行
		 */
		private String optStorageScheduleCron = "0 0 2 15 * ?";
		
		private int optStorageDeleteBeforeDays = 90;
	}

	@Setter
	@Getter
	@ToString
	public static class Security {
		private Jwt jwt = new Jwt();
		private BasicAuth basicAuth = new BasicAuth();

		@Setter
		@Getter
		@ToString
		public static class Jwt {
			private String issuer = "beecomb";
			private String secretKey = "beecomb_jwt@icodegarden";
			private int tokenExpireSeconds = 3600;
		}

		@Setter
		@Getter
		@ToString
		public static class BasicAuth {
			private int maxUserCacheSeconds = 1800;
		}
	}
}
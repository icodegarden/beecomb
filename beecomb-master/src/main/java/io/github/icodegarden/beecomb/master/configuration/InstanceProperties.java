package io.github.icodegarden.beecomb.master.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
@Configuration
@ConfigurationProperties
public class InstanceProperties {

	private Server server = new Server();
	private Job job = new Job();
	private ZooKeeper zookeeper = new ZooKeeper();
	private LoadBalance loadBalance = new LoadBalance();
	private Schedule schedule = new Schedule();
	private Security security = new Security();

	@Data
	public static class Server {
		private String bindIp = SystemUtils.getIp();
		private int port;
		/**
		 * 不健康的sql执行时间
		 */
		private long sqlUnhealthMillis = -1;
		/**
		 * http接口请求优雅停机最大等待
		 */
		private long shutdownGracefullyWaitMillis = 30000;
	}

	@Data
	public static class Job {
		/**
		 * dispatch的过程正常是很快的，但在服务刚启动使用阶段可能会需要更大的延迟（worker需要初始化数据库连接等）
		 */
		private int dispatchTimeoutMillis = 10000;
		private int recoveryScheduleMillis = 60000;
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

	@Data
	public static class Security {
		private Jwt jwt = new Jwt();
		private BasicAuth basicAuth = new BasicAuth();

		@Data
		public static class Jwt {
			private String issuer = "beecomb";
			private String secretKey = "beecomb_jwt@icodegarden";
			private int tokenExpireSeconds = 3600;
		}

		@Data
		public static class BasicAuth {
			private int maxUserCacheSeconds = 1800;
		}
	}
}
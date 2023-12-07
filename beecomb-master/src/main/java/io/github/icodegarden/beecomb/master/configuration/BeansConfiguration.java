package io.github.icodegarden.beecomb.master.configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.zookeeper.client.ZKClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.metrics.job.JobsMetricsOverload;
import io.github.icodegarden.beecomb.master.configuration.InstanceProperties.ZooKeeper;
import io.github.icodegarden.beecomb.master.discovery.InstanceDiscoveryListener;
import io.github.icodegarden.beecomb.master.discovery.ListenableNamesWatchedZooKeeperInstanceDiscovery;
import io.github.icodegarden.beecomb.master.manager.JobRecoveryRecordManager;
import io.github.icodegarden.beecomb.master.manager.TableManager;
import io.github.icodegarden.beecomb.master.schedule.JobRecoverySchedule;
import io.github.icodegarden.beecomb.master.schedule.OptStorageSchedule;
import io.github.icodegarden.beecomb.master.schedule.ReportSchedule;
import io.github.icodegarden.beecomb.master.service.JobFacadeManager;
import io.github.icodegarden.beecomb.master.service.JobReceiver;
import io.github.icodegarden.beecomb.master.service.ReportService;
import io.github.icodegarden.beecomb.master.service.WorkerRemoteService;
import io.github.icodegarden.nutrient.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.nutrient.exchange.loadbalance.MinimumLoadFirstInstanceLoadBalance;
import io.github.icodegarden.nutrient.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.nutrient.lang.lifecycle.CloseableGracefullyShutdown;
import io.github.icodegarden.nutrient.lang.lifecycle.GracefullyShutdown;
import io.github.icodegarden.nutrient.lang.metricsregistry.InstanceDiscovery;
import io.github.icodegarden.nutrient.lang.metricsregistry.InstanceMetrics;
import io.github.icodegarden.nutrient.lang.metricsregistry.InstanceRegistry;
import io.github.icodegarden.nutrient.lang.metricsregistry.Metrics;
import io.github.icodegarden.nutrient.lang.metricsregistry.MetricsOverload;
import io.github.icodegarden.nutrient.lang.metricsregistry.NamesCachedInstanceMetrics;
import io.github.icodegarden.nutrient.lang.query.MysqlTableDataCountCollector;
import io.github.icodegarden.nutrient.lang.query.MysqlTableDataCountStorage;
import io.github.icodegarden.nutrient.lang.query.TableDataCountCollector;
import io.github.icodegarden.nutrient.lang.query.TableDataCountManager;
import io.github.icodegarden.nutrient.lang.query.TableDataCountStorage;
import io.github.icodegarden.nutrient.lang.tuple.NullableTuple2;
import io.github.icodegarden.nutrient.lang.tuple.Tuple2;
import io.github.icodegarden.nutrient.lang.tuple.Tuples;
import io.github.icodegarden.nutrient.shardingsphere.util.DataSourceUtils;
import io.github.icodegarden.nutrient.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.nutrient.zookeeper.concurrent.lock.ZooKeeperLock;
import io.github.icodegarden.nutrient.zookeeper.metricsregistry.ZnodeDataZooKeeperInstanceMetrics;
import io.github.icodegarden.nutrient.zookeeper.metricsregistry.ZnodePatternZooKeeperInstanceDiscovery;
import io.github.icodegarden.nutrient.zookeeper.metricsregistry.ZooKeeperInstanceDiscovery;
import io.github.icodegarden.nutrient.zookeeper.metricsregistry.ZooKeeperInstanceMetrics;
import io.github.icodegarden.nutrient.zookeeper.metricsregistry.ZooKeeperInstanceRegistry;
import io.github.icodegarden.nutrient.zookeeper.metricsregistry.ZooKeeperRegisteredInstance;
import lombok.extern.slf4j.Slf4j;

/**
 * master需要：<br>
 * 自动注册实例@{@link BeansConfiguration#zooKeeperInstanceRegister},{@link ZooKeeperInstanceRegistry#onNewZooKeeper}<br>
 * <br>
 * 定时获取worker的度量缓存数据{@link BeansConfiguration#zooKeeperInstanceMetrics(ZooKeeperHolder)}<br>
 * <br>
 * 开启任务恢复{@link BeansConfiguration#jobRecovery(DistributedLock, JobFacadeManager)},{@link io.github.icodegarden.beecomb.master.schedule.JobRecoverySchedule}<br>
 * 
 * <br>
 * 
 * master不需要： 刷入度量数据<br>
 * 
 * @author Fangfang.Xu
 *
 */
@EnableConfigurationProperties(InstanceProperties.class)
@Configuration
@Slf4j
public class BeansConfiguration {

	@Autowired
	private InstanceProperties instanceProperties;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public TableDataCountManager mysqlTableDataCountManager(DataSource shardingSphereDataSource) {
		DataSource dataSource = DataSourceUtils.firstDataSource((ShardingSphereDataSource) shardingSphereDataSource);

		Set<String> whiteListTables = new HashSet<String>(Arrays.asList("job_main", "job_detail", "delay_job",
				"schedule_job", "job_execute_record", "job_recovery_record", "pending_recovery_job"));

		TableDataCountCollector tableDataCountCollector = new MysqlTableDataCountCollector(dataSource, whiteListTables);
		TableDataCountStorage tableDataCountStorage = new MysqlTableDataCountStorage(dataSource);
		TableDataCountManager tableDataCountManager = new TableDataCountManager(tableDataCountCollector,
				tableDataCountStorage);
		tableDataCountManager.start(5 * 1000, 3600 * 1000);
		return tableDataCountManager;
	}

	/**
	 * 由于配置前缀不同，覆盖
	 */
	@Bean
	public ZooKeeperHolder zooKeeperHolder() {
		ZooKeeperHolder.Config config = new ZooKeeperHolder.Config(instanceProperties.getZookeeper().getConnectString(),
				instanceProperties.getZookeeper().getSessionTimeout(),
				instanceProperties.getZookeeper().getConnectTimeout());
		config.setAclAuth(instanceProperties.getZookeeper().getAclAuth());
		return new ZooKeeperHolder(config);
	}

	/**
	 * 由于配置前缀不同，覆盖
	 */
	@Bean
	public CuratorFramework curatorFramework() {
		ZooKeeper zookeeper = instanceProperties.getZookeeper();

		RetryPolicy retryPolicy = new RetryForever(3000);
		ZKClientConfig zkClientConfig = new ZKClientConfig();
		zkClientConfig.setProperty(ZKClientConfig.ZOOKEEPER_SERVER_PRINCIPAL,
				"zookeeper/" + zookeeper.getConnectString());
		CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeper.getConnectString(),
				zookeeper.getSessionTimeout(), zookeeper.getConnectTimeout(), retryPolicy, zkClientConfig);
		client.start();
		return client;
	}

	@Bean
	public InstanceRegistry<ZooKeeperRegisteredInstance> zooKeeperInstanceRegistry(ZooKeeperHolder zooKeeperHolder) {
		String bindIp = instanceProperties.getServer().getBindIp();
		int port = instanceProperties.getServer().getPort();

		ZooKeeperInstanceRegistry zooKeeperInstanceRegistry = new ZooKeeperInstanceRegistry(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot(), NodeRole.Master.getRoleName(), bindIp, port);
		zooKeeperInstanceRegistry.registerIfNot();

		/**
		 * 从注册中心下线，让客户端的服务发现没有该实例
		 */
		GracefullyShutdown.Registry.singleton()
				.register(new CloseableGracefullyShutdown(zooKeeperInstanceRegistry, "instanceRegistry", -100));

		return zooKeeperInstanceRegistry;
	}

	/**
	 * Metrics缓存刷新方式
	 */
	@Bean
	public InstanceMetrics<Metrics> zooKeeperInstanceMetrics(ZooKeeperHolder zooKeeperHolder) {
		ZooKeeperInstanceMetrics<Metrics> delegator = new ZnodeDataZooKeeperInstanceMetrics(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot());

		NamesCachedInstanceMetrics instanceMetrics = new NamesCachedInstanceMetrics(
				Arrays.asList(NodeRole.Worker.getRoleName()), delegator,
				instanceProperties.getSchedule().getMetricsCacheRefreshIntervalMillis());
		/**
		 * 停止调度
		 */
		GracefullyShutdown.Registry.singleton()
				.register(new CloseableGracefullyShutdown(instanceMetrics, "instanceMetrics", -95));

		return instanceMetrics;
	}

	/**
	 * InstanceDiscovery缓存刷新方式
	 */
	@Bean
	public InstanceDiscovery<ZooKeeperRegisteredInstance> zooKeeperInstanceDiscovery(ZooKeeperHolder zooKeeperHolder,
			List<InstanceDiscoveryListener> instanceDiscoveryListeners) {
		ZooKeeperInstanceDiscovery<ZooKeeperRegisteredInstance> delegator = new ZnodePatternZooKeeperInstanceDiscovery(
				zooKeeperHolder, instanceProperties.getZookeeper().getRoot());

//		NamesWatchedZooKeeperInstanceDiscovery instanceDiscovery = new NamesWatchedZooKeeperInstanceDiscovery(delegator,
//				zooKeeperHolder, instanceProperties.getZookeeper().getRoot(),
//				Arrays.asList(NodeRole.Worker.getRoleName()),
//				instanceProperties.getSchedule().getDiscoveryCacheRefreshIntervalMillis());

		ListenableNamesWatchedZooKeeperInstanceDiscovery instanceDiscovery = new ListenableNamesWatchedZooKeeperInstanceDiscovery(
				delegator, zooKeeperHolder, instanceProperties.getZookeeper().getRoot(),
				Arrays.asList(NodeRole.Worker.getRoleName()),
				instanceProperties.getSchedule().getDiscoveryCacheRefreshIntervalMillis());
		instanceDiscovery.setInstanceDiscoveryListeners(instanceDiscoveryListeners);

		/**
		 * 停止调度
		 */
		GracefullyShutdown.Registry.singleton()
				.register(new CloseableGracefullyShutdown(instanceDiscovery, "instanceDiscovery", -95));

		return instanceDiscovery;
	}

	/**
	 * Master的度量仅用于web查看监控
	 */
	@Bean
	public MetricsOverload jobsMetricsOverload(ZooKeeperHolder zooKeeperHolder, InstanceRegistry instanceRegistry,
			InstanceMetrics instanceMetrics) {
		NullableTuple2<Double, Integer> cpu = null;
		NullableTuple2<Double, Integer> memory = null;
		Tuple2<Integer, Integer> jobs = Tuples.of(0, 0);

		JobsMetricsOverload.Config config = new JobsMetricsOverload.Config(cpu, memory, jobs);
		JobsMetricsOverload jobsMetricsOverload = new JobsMetricsOverload(instanceRegistry, instanceMetrics, config);
		/**
		 * 开启调度刷入度量数据
		 */
		jobsMetricsOverload
				.enableScheduleFlushMetrics(instanceProperties.getSchedule().getFlushMetricsIntervalMillis());
		return jobsMetricsOverload;
	}

	@Bean
	public InstanceLoadBalance minimumLoadFirstInstanceLoadBalance(InstanceDiscovery instanceDiscovery,
			InstanceMetrics<Metrics> instanceMetrics) {
		return new MinimumLoadFirstInstanceLoadBalance(instanceDiscovery, instanceMetrics);
	}

	@Bean
	public JobReceiver jobReceiver(JobFacadeManager jobFacadeManager, WorkerRemoteService remoteService) {
		return new JobReceiver(jobFacadeManager, remoteService);
	}

	@Bean
	public JobRecoverySchedule jobRecoverySchedule(CuratorFramework client, JobFacadeManager jobFacadeManager,
			WorkerRemoteService remoteService, JobRecoveryRecordManager jobRecoveryRecordService) {
		ZooKeeperLock lock = new ZooKeeperLock(client, instanceProperties.getZookeeper().getLockRoot(),
				"JobRecoverySchedule");

		long recoveryScheduleMillis = instanceProperties.getSchedule().getRecoveryScheduleMillis();
		JobRecoverySchedule jobRecoverySchedule = new JobRecoverySchedule(lock, jobFacadeManager, remoteService,
				jobRecoveryRecordService);
		jobRecoverySchedule.scheduleWithFixedDelay(recoveryScheduleMillis, recoveryScheduleMillis);

		return jobRecoverySchedule;
	}

	@Bean
	public ReportSchedule reportSchedule(CuratorFramework client, ReportService reportService) {
		ZooKeeperLock lock = new ZooKeeperLock(client, instanceProperties.getZookeeper().getLockRoot(),
				"ReportSchedule");

		String cron = instanceProperties.getSchedule().getReportScheduleCron();

		ReportSchedule reportSchedule = new ReportSchedule(lock, reportService);
		reportSchedule.scheduleWithCron(cron);
		return reportSchedule;
	}

	@Bean
	public OptStorageSchedule optStorageSpaceSchedule(CuratorFramework client, TableManager tableManager) {
		ZooKeeperLock lock = new ZooKeeperLock(client, instanceProperties.getZookeeper().getLockRoot(),
				"OptStorageSpaceSchedule");

		String cron = instanceProperties.getSchedule().getOptStorageScheduleCron();
		int days = instanceProperties.getSchedule().getOptStorageDeleteBeforeDays();

		OptStorageSchedule optStorageSpaceSchedule = new OptStorageSchedule(lock, days, tableManager);
		optStorageSpaceSchedule.scheduleWithCron(cron);
		return optStorageSpaceSchedule;
	}
}

package io.github.icodegarden.beecomb.master.configuration;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.sql.DataSource;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.client.ZKClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import io.github.icodegarden.beecomb.common.backend.shardingsphere.ApiShardingSphereBuilder;
import io.github.icodegarden.beecomb.common.backend.shardingsphere.BeecombShardingsphereProperties;
import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.metrics.job.JobsMetricsOverload;
import io.github.icodegarden.beecomb.master.configuration.InstanceProperties.ZooKeeper;
import io.github.icodegarden.beecomb.master.discovery.InstanceDiscoveryListener;
import io.github.icodegarden.beecomb.master.discovery.ListenableNamesWatchedZooKeeperInstanceDiscovery;
import io.github.icodegarden.beecomb.master.manager.JobRecoveryRecordManager;
import io.github.icodegarden.beecomb.master.schedule.JobRecoverySchedule;
import io.github.icodegarden.beecomb.master.service.JobFacadeManager;
import io.github.icodegarden.beecomb.master.service.JobReceiver;
import io.github.icodegarden.beecomb.master.service.WorkerRemoteService;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.MinimumLoadFirstInstanceLoadBalance;
import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.endpoint.CloseableGracefullyShutdown;
import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.MetricsOverload;
import io.github.icodegarden.commons.lang.metrics.NamesCachedInstanceMetrics;
import io.github.icodegarden.commons.lang.registry.InstanceDiscovery;
import io.github.icodegarden.commons.lang.registry.InstanceRegistry;
import io.github.icodegarden.commons.lang.tuple.NullableTuple2;
import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.commons.mybatis.interceptor.SqlPerformanceInterceptor;
import io.github.icodegarden.commons.shardingsphere.algorithm.MysqlKeyGenerateAlgorithm;
import io.github.icodegarden.commons.springboot.GracefullyShutdownLifecycle;
import io.github.icodegarden.commons.springboot.SpringContext;
import io.github.icodegarden.commons.springboot.web.filter.ProcessingRequestCountFilter;
import io.github.icodegarden.commons.springboot.web.handler.NativeRestApiExceptionHandler;
import io.github.icodegarden.commons.springboot.web.util.MappingJackson2HttpMessageConverters;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder.Config;
import io.github.icodegarden.commons.zookeeper.concurrent.lock.ZooKeeperLock;
import io.github.icodegarden.commons.zookeeper.metrics.ZnodeDataZooKeeperInstanceMetrics;
import io.github.icodegarden.commons.zookeeper.metrics.ZooKeeperInstanceMetrics;
import io.github.icodegarden.commons.zookeeper.registry.ZnodePatternZooKeeperInstanceDiscovery;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceDiscovery;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceRegistry;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperRegisteredInstance;
import lombok.extern.slf4j.Slf4j;

/**
 * master?????????<br>
 * ??????????????????@{@link BeansConfiguration#zooKeeperInstanceRegister},{@link ZooKeeperInstanceRegistry#onNewZooKeeper}<br>
 * <br>
 * ????????????worker?????????????????????{@link BeansConfiguration#zooKeeperInstanceMetrics(ZooKeeperHolder)}<br>
 * <br>
 * ??????????????????{@link BeansConfiguration#jobRecovery(DistributedLock, JobFacadeManager)},{@link io.github.icodegarden.beecomb.master.schedule.JobRecoverySchedule}<br>
 * 
 * <br>
 * 
 * master???????????? ??????????????????<br>
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Configuration
public class BeansConfiguration {

	@Autowired
	private InstanceProperties instanceProperties;

	@Bean
	public SpringContext springContext() {
		return new SpringContext();
	}

	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		return MappingJackson2HttpMessageConverters.simple();
	}

	@Bean
	public NativeRestApiExceptionHandler nativeRestApiParameterInvalidExceptionHandler() {
		return new NativeRestApiExceptionHandler();
	}

	@Bean
	public SmartLifecycle gracefullyShutdownLifecycle() {
		return new GracefullyShutdownLifecycle();
	}

	@Bean
	public SqlPerformanceInterceptor sqlPerformanceInterceptor() {
		SqlPerformanceInterceptor sqlPerformanceInterceptor = new SqlPerformanceInterceptor();
		sqlPerformanceInterceptor.setFormat(true);
		sqlPerformanceInterceptor.setUnhealthMillis(instanceProperties.getServer().getSqlUnhealthMillis());
		sqlPerformanceInterceptor.setUnhealthSqlConsumer(sql -> {
			log.warn("unhealth sql : {}", sql);
		});
		return sqlPerformanceInterceptor;
	}

	/**
	 * sharding DataSource
	 */
	@Bean
	public DataSource dataSource(BeecombShardingsphereProperties properties) throws SQLException {
		DataSource dataSource = ApiShardingSphereBuilder.getDataSource(properties);

		MysqlKeyGenerateAlgorithm.registerDataSource(dataSource);

		return dataSource;
	}

	@Bean
	public ZooKeeperHolder zooKeeperHolder() {
		Config config = new ZooKeeperHolder.Config(instanceProperties.getZookeeper().getConnectString(),
				instanceProperties.getZookeeper().getSessionTimeout(),
				instanceProperties.getZookeeper().getConnectTimeout());
		config.setAclAuth(instanceProperties.getZookeeper().getAclAuth());
		return new ZooKeeperHolder(config);
	}

	@Bean
	public InstanceRegistry<ZooKeeperRegisteredInstance> zooKeeperInstanceRegistry(ZooKeeperHolder zooKeeperHolder) {
		String bindIp = instanceProperties.getServer().getBindIp();
		int port = instanceProperties.getServer().getPort();

		ZooKeeperInstanceRegistry zooKeeperInstanceRegistry = new ZooKeeperInstanceRegistry(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot(), NodeRole.Master.getRoleName(), bindIp, port);
		zooKeeperInstanceRegistry.registerIfNot();

		/**
		 * ??????????????????????????????????????????????????????????????????
		 */
		GracefullyShutdown.Registry.singleton()
				.register(new CloseableGracefullyShutdown(zooKeeperInstanceRegistry, "instanceRegistry", -100));

		return zooKeeperInstanceRegistry;
	}

	/**
	 * Metrics??????????????????
	 */
	@Bean
	public InstanceMetrics<Metrics> zooKeeperInstanceMetrics(ZooKeeperHolder zooKeeperHolder) {
		ZooKeeperInstanceMetrics<Metrics> delegator = new ZnodeDataZooKeeperInstanceMetrics(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot());

		NamesCachedInstanceMetrics instanceMetrics = new NamesCachedInstanceMetrics(
				Arrays.asList(NodeRole.Worker.getRoleName()), delegator,
				instanceProperties.getSchedule().getMetricsCacheRefreshIntervalMillis());
		/**
		 * ????????????
		 */
		GracefullyShutdown.Registry.singleton()
				.register(new CloseableGracefullyShutdown(instanceMetrics, "instanceMetrics", -95));

		return instanceMetrics;
	}

	/**
	 * InstanceDiscovery??????????????????
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
		 * ????????????
		 */
		GracefullyShutdown.Registry.singleton()
				.register(new CloseableGracefullyShutdown(instanceDiscovery, "instanceDiscovery", -95));

		return instanceDiscovery;
	}

	/**
	 * Master??????????????????web????????????
	 */
	@Bean
	public MetricsOverload jobsMetricsOverload(ZooKeeperHolder zooKeeperHolder, InstanceRegistry instanceRegistry,
			InstanceMetrics instanceMetrics) {
		NullableTuple2<Void, Integer> cpu = null;
		NullableTuple2<Void, Integer> memory = null;
		Tuple2<Integer, Integer> jobs = Tuples.of(0, 0);

		JobsMetricsOverload.Config config = new JobsMetricsOverload.Config(cpu, memory, jobs);
		JobsMetricsOverload jobsMetricsOverload = new JobsMetricsOverload(instanceRegistry, instanceMetrics, config);
		/**
		 * ??????????????????????????????
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
	public JobReceiver jobReceiver(JobFacadeManager jobFacadeManager, WorkerRemoteService remoteService) {
		return new JobReceiver(jobFacadeManager, remoteService);
	}

	@Bean
	public JobRecoverySchedule jobRecovery(CuratorFramework client, JobFacadeManager jobFacadeManager,
			WorkerRemoteService remoteService, JobRecoveryRecordManager jobRecoveryRecordService) {
		ZooKeeperLock lock = new ZooKeeperLock(client, instanceProperties.getZookeeper().getLockRoot(), "JobRecovery");

		JobRecoverySchedule jobRecovery = new JobRecoverySchedule(lock, jobFacadeManager, remoteService,
				jobRecoveryRecordService);
		jobRecovery.start(instanceProperties.getJob().getRecoveryScheduleMillis());

		/**
		 * ????????????
		 */
		GracefullyShutdown.Registry.singleton()
				.register(new CloseableGracefullyShutdown(jobRecovery, "jobRecovery", -90));

		return jobRecovery;
	}

	@Bean
	public FilterRegistrationBean<Filter> FilterRegistrationBean() {
		/**
		 * ????????????
		 */
		ProcessingRequestCountFilter processingRequestCountFilter = new ProcessingRequestCountFilter(Integer.MIN_VALUE,
				instanceProperties.getServer().getShutdownGracefullyWaitMillis());

		FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<Filter>();
		bean.setFilter(processingRequestCountFilter);
		bean.setName("processingRequestCountFilter");
		bean.addUrlPatterns("/*");
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

		GracefullyShutdown.Registry.singleton().register(processingRequestCountFilter);

		return bean;
	}

}

package io.github.icodegarden.beecomb.worker.configuration;

import java.util.Arrays;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.client.ZKClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.beecomb.common.backend.executor.registry.ExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.common.backend.executor.registry.zookeeper.NamesWatchedZooKeeperExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.metrics.job.JobsMetricsOverload;
import io.github.icodegarden.beecomb.common.metrics.job.JobsMetricsOverload.Config;
import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties.Overload;
import io.github.icodegarden.beecomb.worker.core.DelayJobEngine;
import io.github.icodegarden.beecomb.worker.core.JobEngine;
import io.github.icodegarden.beecomb.worker.core.ScheduleJobEngine;
import io.github.icodegarden.beecomb.worker.server.DispatcherHandler;
import io.github.icodegarden.beecomb.worker.server.JobRequestReceiver;
import io.github.icodegarden.beecomb.worker.server.WorkerServer;
import io.github.icodegarden.beecomb.worker.service.DelayJobService;
import io.github.icodegarden.beecomb.worker.service.JobService;
import io.github.icodegarden.beecomb.worker.service.ScheduleJobService;
import io.github.icodegarden.commons.lang.endpoint.CloseableGracefullyShutdown;
import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.lang.metricsregistry.InstanceMetrics;
import io.github.icodegarden.commons.lang.metricsregistry.InstanceRegistry;
import io.github.icodegarden.commons.lang.metricsregistry.Metrics;
import io.github.icodegarden.commons.lang.metricsregistry.MetricsOverload;
import io.github.icodegarden.commons.lang.metricsregistry.NamesCachedInstanceMetrics;
import io.github.icodegarden.commons.lang.tuple.NullableTuple2;
import io.github.icodegarden.commons.lang.tuple.NullableTuples;
import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.metricsregistry.ZnodeDataZooKeeperInstanceMetrics;
import io.github.icodegarden.commons.zookeeper.metricsregistry.ZooKeeperInstanceMetrics;
import io.github.icodegarden.commons.zookeeper.metricsregistry.ZooKeeperInstanceRegistry;
import io.github.icodegarden.commons.zookeeper.metricsregistry.ZooKeeperRegisteredInstance;
import lombok.extern.slf4j.Slf4j;

/**
 * worker需要：<br>
 * 自动注册实例{@link BeansConfiguration#zooKeeperInstanceRegistry},{@link ZooKeeperInstanceRegistry#onNewZooKeeper}<br>
 * 
 * 定时刷新executor的度量数据缓存{@link BeansConfiguration#zooKeeperInstanceMetrics(ZooKeeperHolder)}<br>
 * 
 * 实时刷入度量数据@{@link BeansConfiguration#jobsMetricsOverload}
 * worker允许实时刷入的原因是worker的job负载数据只有新的任务enQueue以及任务end的时候有变化，这种情况对worker来说不算很频繁，可以实时刷入<br>
 * 
 * 自动刷入度量数据（当zk session重新建立后）{@link BeansConfiguration#jobsMetricsOverload}<br>
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
//		client.start();没用到，不启动
		return client;
	}

	@Bean
	public InstanceRegistry<ZooKeeperRegisteredInstance> zooKeeperInstanceRegistry(ZooKeeperHolder zooKeeperHolder) {
		String bindIp = instanceProperties.getServer().getBindIp();
		int port = instanceProperties.getServer().getPort();

		ZooKeeperInstanceRegistry zooKeeperInstanceRegistry = new ZooKeeperInstanceRegistry(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot(), NodeRole.Worker.getRoleName(), bindIp, port);
		zooKeeperInstanceRegistry.registerIfNot();

		/**
		 * 从注册中心下线，让客户端的服务发现没有该实例
		 */
		GracefullyShutdown.Registry.singleton()
				.register(new CloseableGracefullyShutdown(zooKeeperInstanceRegistry, "instanceRegistry", -100));

		return zooKeeperInstanceRegistry;
	}

	/**
	 * Executor的Metrics缓存刷新方式
	 */
	@Bean
	public InstanceMetrics<Metrics> zooKeeperInstanceMetrics(ZooKeeperHolder zooKeeperHolder) {
		ZooKeeperInstanceMetrics<Metrics> delegator = new ZnodeDataZooKeeperInstanceMetrics(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot());

		NamesCachedInstanceMetrics instanceMetrics = new NamesCachedInstanceMetrics(
				Arrays.asList(NodeRole.Executor.getRoleName()), delegator,
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
	public ExecutorInstanceDiscovery zooKeeperExecutorInstanceDiscovery(ZooKeeperHolder zooKeeperHolder) {
		ExecutorInstanceDiscovery executorInstanceDiscovery = new NamesWatchedZooKeeperExecutorInstanceDiscovery(
				zooKeeperHolder, instanceProperties.getZookeeper().getRoot(),
				instanceProperties.getSchedule().getDiscoveryCacheRefreshIntervalMillis());

		/**
		 * 停止调度
		 */
		GracefullyShutdown.Registry.singleton()
				.register(new CloseableGracefullyShutdown(executorInstanceDiscovery, "executorInstanceDiscovery", -95));

		return executorInstanceDiscovery;
	}

	@Bean
	public MetricsOverload jobsMetricsOverload(ZooKeeperHolder zooKeeperHolder, InstanceRegistry instanceRegistry,
			InstanceMetrics instanceMetrics) {
		Overload overload = instanceProperties.getOverload();
		NullableTuple2<Double, Integer> cpu = overload.getCpu() != null
				? NullableTuples.of(overload.getCpu().getMax(), overload.getCpu().getWeight())
				: null;
		NullableTuple2<Double, Integer> memory = overload.getMemory() != null
				? NullableTuples.of(overload.getMemory().getMax(), overload.getMemory().getWeight())
				: null;
		Tuple2<Integer, Integer> jobs = Tuples.of(overload.getJobs().getMax(), overload.getJobs().getWeight());

		Config config = new JobsMetricsOverload.Config(cpu, memory, jobs);
		JobsMetricsOverload jobsMetricsOverload = new JobsMetricsOverload(instanceRegistry, instanceMetrics, config);
		/**
		 * 开启调度刷入度量数据
		 */
		jobsMetricsOverload
				.enableScheduleFlushMetrics(instanceProperties.getSchedule().getFlushMetricsIntervalMillis());
		zooKeeperHolder.addNewZooKeeperListener(() -> {
			/**
			 * 保障zk重新建立session后能够自动刷入metrics
			 */
			jobsMetricsOverload.flushMetrics();
		});
		return jobsMetricsOverload;
	}

	@Bean("delay")
	public JobEngine delayJobEngine(ExecutorInstanceDiscovery executorInstanceDiscovery,
			InstanceMetrics instanceMetrics, MetricsOverload jobOverload, DelayJobService delayJobStorage) throws Exception {
		DelayJobEngine jobEngine = new DelayJobEngine(executorInstanceDiscovery, instanceMetrics, jobOverload, delayJobStorage,
				instanceProperties);
//		/**
//		 * TODO remove
//		 */
//		if(System.getProperty("executor.serializer.kryo") != null) {
//			log.warn("executor use serializer of kryo");
//			
//			NioClientPool nioClientPool = NioClientPool.newPool(NodeRole.Worker.getRoleName(), (ip, port) -> {
//				ClientNioSelector CLIENT_NIO_SELECTOR = ClientNioSelector.openNew(NodeRole.Worker.getRoleName());
//				JavaNioClient nioClient = new JavaNioClient(new InetSocketAddress(ip, port), CLIENT_NIO_SELECTOR);
//				nioClient.setSerializerType(SerializerType.Kryo);
//				return nioClient;
//			});
//			NioProtocol protocol = new NioProtocol(nioClientPool);
//			
//			Field field = AbstractJobEngine.class.getDeclaredField("protocol");
//			boolean accessible = field.isAccessible();
//			field.setAccessible(true);
//			field.set(jobEngine, protocol);
//			field.setAccessible(accessible);
//		}
		
		return jobEngine;
	}

	@Bean("schedule")
	public JobEngine scheduleJobEngine(ExecutorInstanceDiscovery executorInstanceDiscovery,
			InstanceMetrics instanceMetrics, MetricsOverload jobOverload, ScheduleJobService scheduleJobStorage) throws Exception {
		ScheduleJobEngine jobEngine = new ScheduleJobEngine(executorInstanceDiscovery, instanceMetrics, jobOverload, scheduleJobStorage,
				instanceProperties);
//		/**
//		 * TODO remove
//		 */
//		if(System.getProperty("executor.serializer.kryo") != null) {
//			log.warn("executor use serializer of kryo");
//			
//			NioClientPool nioClientPool = NioClientPool.newPool(NodeRole.Worker.getRoleName(), (ip, port) -> {
//				ClientNioSelector CLIENT_NIO_SELECTOR = ClientNioSelector.openNew(NodeRole.Worker.getRoleName());
//				JavaNioClient nioClient = new JavaNioClient(new InetSocketAddress(ip, port), CLIENT_NIO_SELECTOR);
//				nioClient.setSerializerType(SerializerType.Kryo);
//				return nioClient;
//			});
//			NioProtocol protocol = new NioProtocol(nioClientPool);
//			
//			Field field = AbstractJobEngine.class.getDeclaredField("protocol");
//			boolean accessible = field.isAccessible();
//			field.setAccessible(true);
//			field.set(jobEngine, protocol);
//			field.setAccessible(accessible);
//		}
		
		return jobEngine;
	}

	@Bean
	public JobRequestReceiver jobReceiver(JobService jobStorage, JobEngine jobEngine) {
		return new JobRequestReceiver(jobStorage, jobEngine);
	}

	@Bean
	public DispatcherHandler dispatcherHandler(JobRequestReceiver jobReceiver, JobEngine jobEngine) {
		return new DispatcherHandler(jobReceiver, jobEngine);
	}

	/**
	 * 测试时不开启
	 */
	@ConditionalOnExpression("'${server.env:normal}'!='junit'")
	@Bean
	public WorkerServer workerServer(DispatcherHandler dispatcherHandler) {
		return new WorkerServer(instanceProperties, dispatcherHandler);
	}

}

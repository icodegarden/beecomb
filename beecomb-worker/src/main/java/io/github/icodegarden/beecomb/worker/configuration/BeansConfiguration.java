package io.github.icodegarden.beecomb.worker.configuration;

import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.beecomb.common.backend.executor.registry.ExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.common.backend.executor.registry.zookeeper.NamesWatchedZooKeeperExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.common.backend.shardingsphere.ApiShardingSphereBuilder;
import io.github.icodegarden.beecomb.common.backend.shardingsphere.BeecombShardingsphereProperties;
import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.metrics.job.JobsMetricsOverload;
import io.github.icodegarden.beecomb.common.metrics.job.JobsMetricsOverload.Config;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties.Overload;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties.Schedule;
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
import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.MetricsOverload;
import io.github.icodegarden.commons.lang.metrics.NamesCachedInstanceMetrics;
import io.github.icodegarden.commons.lang.registry.InstanceRegistry;
import io.github.icodegarden.commons.lang.tuple.NullableTuple2;
import io.github.icodegarden.commons.lang.tuple.NullableTuples;
import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.commons.mybatis.interceptor.SqlPerformanceInterceptor;
import io.github.icodegarden.commons.shardingsphere.algorithm.MysqlKeyGenerateAlgorithm;
import io.github.icodegarden.commons.springboot.GracefullyShutdownLifecycle;
import io.github.icodegarden.commons.springboot.SpringContext;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.metrics.ZnodeDataZooKeeperInstanceMetrics;
import io.github.icodegarden.commons.zookeeper.metrics.ZooKeeperInstanceMetrics;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceRegistry;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperRegisteredInstance;
import lombok.extern.slf4j.Slf4j;

/**
 * worker?????????<br>
 * ??????????????????{@link BeansConfiguration#zooKeeperInstanceRegister},{@link ZooKeeperInstanceRegistry#onNewZooKeeper}<br>
 * 
 * ????????????executor?????????????????????{@link BeansConfiguration#zooKeeperInstanceMetrics(ZooKeeperHolder)}<br>
 * 
 * ????????????????????????@{@link BeansConfiguration#jobsMetricsOverload}
 * worker??????????????????????????????worker???job??????????????????????????????enQueue????????????end????????????????????????????????????worker??????????????????????????????????????????<br>
 * 
 * ??????????????????????????????zk session??????????????????{@link BeansConfiguration#jobsMetricsOverload}<br>
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
		ZooKeeperHolder.Config config = new ZooKeeperHolder.Config(instanceProperties.getZookeeper().getConnectString(),
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
				instanceProperties.getZookeeper().getRoot(), NodeRole.Worker.getRoleName(), bindIp, port);
		zooKeeperInstanceRegistry.registerIfNot();

		/**
		 * ??????????????????????????????????????????????????????????????????
		 */
		GracefullyShutdown.Registry.singleton()
				.register(new CloseableGracefullyShutdown(zooKeeperInstanceRegistry, "instanceRegistry", -100));

		return zooKeeperInstanceRegistry;
	}

	/**
	 * Executor???Metrics??????????????????
	 */
	@Bean
	public InstanceMetrics<Metrics> zooKeeperInstanceMetrics(ZooKeeperHolder zooKeeperHolder) {
		ZooKeeperInstanceMetrics<Metrics> delegator = new ZnodeDataZooKeeperInstanceMetrics(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot());

		NamesCachedInstanceMetrics instanceMetrics = new NamesCachedInstanceMetrics(
				Arrays.asList(NodeRole.Executor.getRoleName()), delegator,
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
	public ExecutorInstanceDiscovery zooKeeperExecutorInstanceDiscovery(ZooKeeperHolder zooKeeperHolder) {
		ExecutorInstanceDiscovery executorInstanceDiscovery = new NamesWatchedZooKeeperExecutorInstanceDiscovery(
				zooKeeperHolder, instanceProperties.getZookeeper().getRoot(),
				instanceProperties.getSchedule().getDiscoveryCacheRefreshIntervalMillis());

		/**
		 * ????????????
		 */
		GracefullyShutdown.Registry.singleton()
				.register(new CloseableGracefullyShutdown(executorInstanceDiscovery, "executorInstanceDiscovery", -95));

		return executorInstanceDiscovery;
	}

	@Bean
	public MetricsOverload jobsMetricsOverload(ZooKeeperHolder zooKeeperHolder, InstanceRegistry instanceRegistry,
			InstanceMetrics instanceMetrics) {
		Overload overload = instanceProperties.getOverload();
		NullableTuple2<Void, Integer> cpu = overload.getCpu() != null
				? NullableTuples.of(null, overload.getCpu().getWeight())
				: null;
		NullableTuple2<Void, Integer> memory = overload.getMemory() != null
				? NullableTuples.of(null, overload.getMemory().getWeight())
				: null;
		Tuple2<Integer, Integer> jobs = Tuples.of(overload.getJobs().getMax(), overload.getJobs().getWeight());

		Config config = new JobsMetricsOverload.Config(cpu, memory, jobs);
		JobsMetricsOverload jobsMetricsOverload = new JobsMetricsOverload(instanceRegistry, instanceMetrics, config);
		/**
		 * ??????????????????????????????
		 */
		jobsMetricsOverload
				.enableScheduleFlushMetrics(instanceProperties.getSchedule().getFlushMetricsIntervalMillis());
		zooKeeperHolder.addNewZooKeeperListener(() -> {
			/**
			 * ??????zk????????????session?????????????????????metrics
			 */
			jobsMetricsOverload.flushMetrics();
		});
		return jobsMetricsOverload;
	}

	@Bean("delay")
	public JobEngine delayJobEngine(ExecutorInstanceDiscovery executorInstanceDiscovery,
			InstanceMetrics instanceMetrics, MetricsOverload jobOverload, DelayJobService delayJobStorage) {
		return new DelayJobEngine(executorInstanceDiscovery, instanceMetrics, jobOverload, delayJobStorage,
				instanceProperties);
	}

	@Bean("schedule")
	public JobEngine scheduleJobEngine(ExecutorInstanceDiscovery executorInstanceDiscovery,
			InstanceMetrics instanceMetrics, MetricsOverload jobOverload, ScheduleJobService scheduleJobStorage) {
		return new ScheduleJobEngine(executorInstanceDiscovery, instanceMetrics, jobOverload, scheduleJobStorage,
				instanceProperties);
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
	 * ??????????????????
	 */
	@ConditionalOnExpression("'${test.server.enabled:true}'!='false'")
	@Bean
	public WorkerServer workerServer(DispatcherHandler dispatcherHandler) {
		return new WorkerServer(instanceProperties, dispatcherHandler);
	}

}

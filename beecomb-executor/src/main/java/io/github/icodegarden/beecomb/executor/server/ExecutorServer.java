package io.github.icodegarden.beecomb.executor.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.metrics.job.JobsMetricsOverload;
import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import io.github.icodegarden.beecomb.executor.ExecutorException;
import io.github.icodegarden.beecomb.executor.InstanceProperties;
import io.github.icodegarden.beecomb.executor.InstanceProperties.Overload;
import io.github.icodegarden.beecomb.executor.InstanceProperties.Server;
import io.github.icodegarden.beecomb.executor.ZooKeeperSupportInstanceProperties;
import io.github.icodegarden.beecomb.executor.registry.JobHandlerRegistry;
import io.github.icodegarden.beecomb.executor.registry.zookeeper.ZooKeeperJobHandlerRegistry;
import io.github.icodegarden.commons.lang.concurrent.NamedThreadFactory;
import io.github.icodegarden.commons.lang.endpoint.CloseableGracefullyShutdown;
import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.registry.InstanceRegistry;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
import io.github.icodegarden.commons.lang.tuple.NullableTuple2;
import io.github.icodegarden.commons.lang.tuple.NullableTuples;
import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.java.JavaNioServer;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.metrics.ZnodeDataZooKeeperInstanceMetrics;
import io.github.icodegarden.commons.zookeeper.metrics.ZooKeeperInstanceMetrics;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceRegistry;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ExecutorServer implements GracefullyShutdown {
//	private static final Logger log = LoggerFactory.getLogger(ExecutorServer.class);

	private final InstanceProperties instanceProperties;
	private final JobHandlerRegistry jobHandlerRegistry;
	private final InstanceRegistry<? extends RegisteredInstance> instanceRegistry;
	private final InstanceMetrics<? extends Metrics> instanceMetrics;
	private DispatcherHandler dispatcherHandler;
	private EntryMessageHandler entryMessageHandler;
	private NioServer nioServer;

	/**
	 * start a new Server
	 * 
	 * @param executorName
	 * @param config
	 * @return
	 * @throws ExecutorException
	 */
	public ExecutorServer(String executorName, ZooKeeperSupportInstanceProperties instanceProperties)
			throws ExecutorException {
		try {
			this.instanceProperties = instanceProperties;

			ZooKeeper zookeeper = instanceProperties.getZookeeper();
			Server server = instanceProperties.getServer();

			ZooKeeperHolder.Config config = new ZooKeeperHolder.Config(zookeeper.getConnectString(),
					zookeeper.getSessionTimeout(), zookeeper.getConnectTimeout());
			config.setAclAuth(zookeeper.getAclAuth());
			ZooKeeperHolder zooKeeperHolder = new ZooKeeperHolder(config);

			ZooKeeperInstanceRegistry zooKeeperInstanceRegistry = prepareZooKeeperInstanceRegistry(instanceProperties,
					server.getExecutorPort(), zookeeper.getRoot(), zooKeeperHolder);

			ZooKeeperInstanceMetrics<Metrics> zooKeeperInstanceMetrics = new ZnodeDataZooKeeperInstanceMetrics(
					zooKeeperHolder, zookeeper.getRoot());

			this.jobHandlerRegistry = prepareJobHandlerRegistry(executorName, zooKeeperHolder,
					zooKeeperInstanceRegistry);

			JobsMetricsOverload jobsMetricsOverload = prepareJobOverload(instanceProperties, zooKeeperInstanceRegistry,
					zooKeeperInstanceMetrics, zooKeeperHolder);

			JobReceiver jobReceiver = new JobReceiver(jobHandlerRegistry, jobsMetricsOverload);
			this.dispatcherHandler = new DispatcherHandler(jobReceiver);

			startNioServer(instanceProperties);

			GracefullyShutdown.Registry.singleton()
					.register(new CloseableGracefullyShutdown(zooKeeperInstanceRegistry, "instanceRegistry", -100));
			GracefullyShutdown.Registry.singleton()
					.register(new CloseableGracefullyShutdown(zooKeeperInstanceMetrics, "instanceMetrics", -90));
			GracefullyShutdown.Registry.singleton()
					.register(new CloseableGracefullyShutdown(jobsMetricsOverload, "metricsOverload", -80));

			GracefullyShutdown.Registry.singleton().register(this);

			this.instanceRegistry = zooKeeperInstanceRegistry;
			this.instanceMetrics = zooKeeperInstanceMetrics;
		} catch (Throwable e) {
			throw new ExecutorException("ex on start executor", e);
		}
	}

	private ZooKeeperInstanceRegistry prepareZooKeeperInstanceRegistry(ZooKeeperSupportInstanceProperties config,
			final int port, final String root, ZooKeeperHolder zooKeeperHolder) {
		Server server = config.getServer();
		ZooKeeperInstanceRegistry zooKeeperInstanceRegistry = new ZooKeeperInstanceRegistry(zooKeeperHolder, root,
				NodeRole.Executor.getRoleName(), server.getExecutorIp(), port);
		/**
		 * prepare instance register
		 */
		zooKeeperInstanceRegistry.registerIfNot();
		return zooKeeperInstanceRegistry;
	}

	private JobHandlerRegistry prepareJobHandlerRegistry(String executorName, ZooKeeperHolder zooKeeperHolder,
			ZooKeeperInstanceRegistry zooKeeperInstanceRegistry) {
		return new ZooKeeperJobHandlerRegistry(executorName, zooKeeperHolder, zooKeeperInstanceRegistry);
	}

	private JobsMetricsOverload prepareJobOverload(ZooKeeperSupportInstanceProperties instanceProperties,
			InstanceRegistry<? extends RegisteredInstance> instanceRegistry,
			InstanceMetrics<? extends Metrics> instanceMetrics, ZooKeeperHolder zooKeeperHolder) {
		Overload overload = instanceProperties.getOverload();
		NullableTuple2<Void, Integer> cpu = overload.getCpu() != null
				? NullableTuples.of(null, overload.getCpu().getWeight())
				: null;
		NullableTuple2<Void, Integer> memory = overload.getMemory() != null
				? NullableTuples.of(null, overload.getMemory().getWeight())
				: null;
		Tuple2<Integer, Integer> jobs = Tuples.of(overload.getJobs().getMax(), overload.getJobs().getWeight());
		JobsMetricsOverload.Config overloadConfig = new JobsMetricsOverload.Config(cpu, memory, jobs);

		JobsMetricsOverload jobsMetricsOverload = new JobsMetricsOverload(instanceRegistry, instanceMetrics,
				overloadConfig);
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

	private void startNioServer(ZooKeeperSupportInstanceProperties config) throws IOException {
		Server server = config.getServer();

		this.entryMessageHandler = new EntryMessageHandler(dispatcherHandler);

		JavaNioServer javaNioServer = new JavaNioServer("Executor-NioServer",
				new InetSocketAddress(server.getExecutorIp(), server.getExecutorPort()), entryMessageHandler);

		ThreadPoolExecutor workerThreadPool = new ThreadPoolExecutor(server.getMinWorkerThreads(),
				server.getMaxWorkerThreads(), 120, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100),
				new NamedThreadFactory("Nio-ExecutorServer"), new ThreadPoolExecutor.CallerRunsPolicy());
		javaNioServer.setWorkerThreadPool(workerThreadPool);

		nioServer = javaNioServer;

		nioServer.start();
	}

	public InstanceRegistry<? extends RegisteredInstance> getInstanceRegistry() {
		return instanceRegistry;
	}

	public JobHandlerRegistry getJobHandlerRegistry() {
		return jobHandlerRegistry;
	}

	@Override
	public String shutdownName() {
		return "executorServer";
	}

	/**
	 * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????server
	 */
	@Override
	public void shutdown() {
		entryMessageHandler.closeBlocking(instanceProperties.getServer().getNioServerShutdownBlockingTimeoutMillis());

		try {
			nioServer.close();
		} catch (IOException e) {
			throw new ExecutorException("ex on close nioServer", e);
		}
	}

	@Override
	public int shutdownOrder() {
		return Integer.MAX_VALUE;
	}

}

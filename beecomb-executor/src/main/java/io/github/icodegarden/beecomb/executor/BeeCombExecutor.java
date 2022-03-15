package io.github.icodegarden.beecomb.executor;

import java.io.Closeable;
import java.util.Collection;

import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.beecomb.executor.registry.JobHandlerRegistry;
import io.github.icodegarden.beecomb.executor.registry.zookeeper.ZooKeeperJobHandlerRegistry;
import io.github.icodegarden.beecomb.executor.server.ExecutorServer;
import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceRegistry;

/**
 * Executor需要：<br>
 * 自动注册实例{@link ExecutorServer#prepareZooKeeperInstanceRegistry},{@link ZooKeeperInstanceRegistry#onNewZooKeeper}<br>
 * 
 * 自动刷入JobHandler的注册信息（当zk
 * session重新建立后）{@link ExecutorServer#prepareJobHandlerRegistry},{@link ZooKeeperJobHandlerRegistry#onNewZooKeeper()}<br>
 * 
 * 定时刷入度量数据{@link ExecutorServer#prepareJobOverload},Executor使用定时刷入的原因是Executor的job负载数据变化在任务数量庞大和执行频率高时比较频繁<br>
 * 
 * 自动刷入度量数据（当zk session重新建立后）{@link ExecutorServer#prepareJobOverload}<br>
 * 
 * <br>
 * 
 * Executor不需要：获取度量数据<br>
 * 
 * 
 * @author Fangfang.Xu
 *
 */
public class BeeCombExecutor implements JobHandlerRegistry, Closeable {
//	private static final Logger log = LoggerFactory.getLogger(BeeCombExecutor.class);

	private final JobHandlerRegistry delegator;
	
	private BeeCombExecutor(JobHandlerRegistry delegator, ExecutorServer executorServer) {
		this.delegator = delegator;
	}

	/**
	 * start a new Executor instance
	 * 
	 * @param executorName
	 * @param config
	 * @return
	 * @throws ExecutorException
	 */
	public static BeeCombExecutor start(String executorName, ZooKeeperSupportInstanceProperties instanceProperties)
			throws ExecutorException {
		ExecutorServer executorServer = new ExecutorServer(executorName, instanceProperties);
		BeeCombExecutor beeCombExecutor = new BeeCombExecutor(executorServer.getJobHandlerRegistry(), executorServer);
		return beeCombExecutor;
	}

	/**
	 * 优雅停机<br>
	 */
	@Override
	public void close() throws ExecutorException {
		GracefullyShutdown.Registry.singleton().shutdownRegistered();
	}

	@Override
	public void registerAppend(Collection<? extends JobHandler> jobHandlers) {
		delegator.registerAppend(jobHandlers);
	}

	@Override
	public void registerReplace(Collection<? extends JobHandler> jobHandlers) {
		delegator.registerReplace(jobHandlers);
	}

	@Override
	public JobHandler getJobHandler(String jobHandlerName) {
		return delegator.getJobHandler(jobHandlerName);
	}

	@Override
	public Collection<? extends JobHandler> listJobHandlers() {
		return delegator.listJobHandlers();
	}

	@Override
	public void deregister(Collection<? extends JobHandler> jobHandlers) {
		delegator.deregister(jobHandlers);
	}

	@Override
	public void deregisterAll() {
		delegator.deregisterAll();
	}
}

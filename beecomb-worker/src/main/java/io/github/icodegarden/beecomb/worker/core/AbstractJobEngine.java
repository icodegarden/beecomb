package io.github.icodegarden.beecomb.worker.core;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.worker.exception.ExceedExpectedJobEngineException;
import io.github.icodegarden.beecomb.worker.exception.ExceedOverloadJobEngineException;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.commons.exchange.ParallelExchanger;
import io.github.icodegarden.commons.exchange.ParallelLoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.loadbalance.EmptyInstanceLoadBalance;
import io.github.icodegarden.commons.exchange.nio.NioProtocol;
import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.lang.metrics.MetricsOverload;
import io.github.icodegarden.commons.lang.result.Result3;
import io.github.icodegarden.commons.lang.result.Results;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.nio.java.ClientNioSelector;
import io.github.icodegarden.commons.nio.java.JavaNioClient;
import io.github.icodegarden.commons.nio.pool.NioClientPool;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class AbstractJobEngine implements JobEngine, GracefullyShutdown {
	static NioProtocol protocol_for_Test;

	private static final ClientNioSelector CLIENT_NIOSELECTOR = ClientNioSelector
			.openNew(NodeRole.Worker.getRoleName());

	protected NioProtocol protocol;

	protected MetricsOverload metricsOverload;
	protected InstanceProperties instanceProperties;
	protected Map<Long/* jobId */, JobTrigger> queuedJobs = new HashMap<Long, JobTrigger>();

	protected ParallelLoadBalanceExchanger parallelLoadBalanceExchanger;

	public AbstractJobEngine(MetricsOverload metricsOverload, InstanceProperties instanceProperties) {
		this.metricsOverload = metricsOverload;
		this.instanceProperties = instanceProperties;

		if (protocol_for_Test != null) {
			this.protocol = protocol_for_Test;
		} else {
			NioClientPool nioClientPool = NioClientPool.newPool(NodeRole.Master.getRoleName(), (ip, port) -> {
				return new JavaNioClient(new InetSocketAddress(ip, port), CLIENT_NIOSELECTOR);
			});
			this.protocol = new NioProtocol(nioClientPool);
		}

		ParallelExchanger.Config config = new ParallelExchanger.Config(
				instanceProperties.getLoadBalance().getMaxCandidates(), 64,
				instanceProperties.getOverload().getJobs().getMax());
		parallelLoadBalanceExchanger = new ParallelLoadBalanceExchanger(this.protocol, new EmptyInstanceLoadBalance(),
				NodeRole.Executor.getRoleName(), config);

		GracefullyShutdown.Registry.singleton().register(this);
	}

	@Override
	public boolean allowEnQueue(ExecutableJobBO job) {
		return !metricsOverload.willOverload(job);
	}

	@Override
	public Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueue(ExecutableJobBO job) {
		if (metricsOverload.incrementOverload(job)) {
			Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueueResult = doEnQueue(job);
			if (!enQueueResult.isSuccess()) {
				JobEngineException exception = enQueueResult.getT3();
				log.warn("job doEnQueue not success, reason:{}, job:{}", exception.getReason(), job);
				metricsOverload.decrementOverload(job);
				return enQueueResult;
			}

			try {
				metricsOverload.flushMetrics();
				/**
				 * 在任务进队列时把queuedAt，queuedAtInstance等参数填入，因为后续不会再查数据库，触发查库的是恢复的任务
				 */
				job.setQueuedAt(SystemUtils.now());
				InstanceProperties instanceProperties = InstanceProperties.singleton();
				job.setQueuedAtInstance(SystemUtils.formatIpPort(instanceProperties.getServer().getBindIp(),
						instanceProperties.getServer().getPort()));

				queuedJobs.put(job.getId(), enQueueResult.getT2());

				return enQueueResult;
			} catch (Exception e) {
				// 取消enQueue
				removeQueue(enQueueResult);
				return Results.of(false, job, null, new ExceedExpectedJobEngineException(e));
			}
		}
		return Results.of(false, job, null,
				new ExceedOverloadJobEngineException("metrics will overload", metricsOverload.getLocalMetrics()));
	}

	/**
	 * 不会抛出异常
	 * 
	 * @param job
	 * @return ExecutableJobBO:enQueue的job，JobTrigger：挂载的对象，JobEngineException:如果有意外发生
	 */
	protected abstract Result3<ExecutableJobBO, JobTrigger, JobEngineException> doEnQueue(ExecutableJobBO job);

	@Override
	public boolean removeQueue(ExecutableJobBO job) {
		JobTrigger jobTrigger = queuedJobs.get(job.getId());
		if (jobTrigger == null) {
			/**
			 * 已不存在
			 */
			return true;
		}
		Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueueResult = Results.of(true, job, jobTrigger,
				null);
		return removeQueue(enQueueResult);
	}

	/**
	 * 
	 * @param enQueueResult 与enQueue结果保持一致
	 * @return
	 */
	protected abstract boolean removeQueue(Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueueResult);

//	/**
//	 * 每次任务的结果更新到 内存job, 以便下次触发时相关字段参数是正确的而不用查库
//	 * 
//	 * @param job
//	 * @return
//	 */
//	protected Consumer<JobFreshParams> buildJobFreshParamsCallback(ExecutableJobBO job) {
//		return params -> {
//			job.setLastExecuteExecutor(params.getLastExecuteExecutor());// 只在成功时有参数
//			job.setLastExecuteReturns(params.getLastExecuteReturns());// 只在成功时有参数
//			job.setLastExecuteSuccess(params.isLastExecuteSuccess());
//			job.setLastTrigAt(params.getLastTrigAt());
//			job.setLastTrigResult(params.getLastTrigResult());
//		};
//	}

	@Override
	public void shutdown() {
		/**
		 * 停止引擎
		 */
		shutdownBlocking(instanceProperties.getServer().getEngineShutdownBlockingTimeoutMillis());
	}

	@Override
	public int shutdownOrder() {
		return -80;
	}
}

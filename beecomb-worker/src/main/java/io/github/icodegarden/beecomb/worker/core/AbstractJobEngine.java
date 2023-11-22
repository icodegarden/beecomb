package io.github.icodegarden.beecomb.worker.core;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.transfer.RequestExecutorDTO;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.worker.exception.ExceedExpectedJobEngineException;
import io.github.icodegarden.beecomb.worker.exception.ExceedOverloadJobEngineException;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.beecomb.worker.loadbalance.ExecutorInstanceLoadBalance;
import io.github.icodegarden.beecomb.worker.service.JobService;
import io.github.icodegarden.nutrient.exchange.CandidatesSwitchableLoadBalanceExchanger;
import io.github.icodegarden.nutrient.exchange.ParallelExchangeResult;
import io.github.icodegarden.nutrient.exchange.ParallelExchanger;
import io.github.icodegarden.nutrient.exchange.ParallelLoadBalanceExchanger;
import io.github.icodegarden.nutrient.exchange.loadbalance.EmptyInstanceLoadBalance;
import io.github.icodegarden.nutrient.exchange.nio.NioProtocol;
import io.github.icodegarden.nutrient.lang.lifecycle.GracefullyShutdown;
import io.github.icodegarden.nutrient.lang.metricsregistry.MetricsOverload;
import io.github.icodegarden.nutrient.lang.result.Result3;
import io.github.icodegarden.nutrient.lang.result.Results;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;
import io.github.icodegarden.nutrient.nio.SerializerType;
import io.github.icodegarden.nutrient.nio.java.ClientNioSelector;
import io.github.icodegarden.nutrient.nio.java.JavaNioClient;
import io.github.icodegarden.nutrient.nio.pool.NioClientPool;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class AbstractJobEngine implements JobEngine, GracefullyShutdown {
	static NioProtocol protocol_for_Test;

	private static final ClientNioSelector CLIENT_NIO_SELECTOR = ClientNioSelector
			.openNew(NodeRole.Worker.getRoleName());

	protected NioProtocol protocol;

	private final JobService jobService;
	protected MetricsOverload metricsOverload;
	protected InstanceProperties instanceProperties;
	protected JobQueue jobQueue;

	protected ParallelLoadBalanceExchanger parallelLoadBalanceExchanger;

	public AbstractJobEngine(JobService jobService, MetricsOverload metricsOverload,
			InstanceProperties instanceProperties, JobQueue jobQueue) {
		this.jobService = jobService;
		this.metricsOverload = metricsOverload;
		this.instanceProperties = instanceProperties;
		this.jobQueue = jobQueue;

		if (protocol_for_Test != null) {
			this.protocol = protocol_for_Test;
		} else {
			NioClientPool nioClientPool = NioClientPool.newPool(NodeRole.Worker.getRoleName(), (ip, port) -> {
				JavaNioClient nioClient = new JavaNioClient(new InetSocketAddress(ip, port), CLIENT_NIO_SELECTOR);
				nioClient.setSerializerType(SerializerType.Hessian2);
				return nioClient;
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
	public boolean isQueued(ExecutableJobBO job) {
		JobTrigger jobTrigger = jobQueue.getJobTrigger(job.getId());
		return jobTrigger != null;
	}

	@Override
	public boolean allowEnQueue(ExecutableJobBO job) {
		return !metricsOverload.willOverload(job);
	}

	@Override
	public Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueue(ExecutableJobBO job) {
		/**
		 * 容错
		 */
		JobTrigger jobTrigger = jobQueue.getJobTrigger(job.getId());
		if (jobTrigger != null) {
			return Results.of(true, job, jobTrigger, null);
		}

		if (!metricsOverload.incrementOverload(job)) {
			return Results.of(false, job, null,
					new ExceedOverloadJobEngineException("metrics will overload", metricsOverload.getLocalMetrics()));
		}

		Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueueResult = doEnQueue(job);
		if (!enQueueResult.isSuccess()) {
			JobEngineException exception = enQueueResult.getT3();
			log.warn("job doEnQueue failed, reason:{}, job:{}", exception.getReason(), job);
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

			return enQueueResult;
		} catch (Exception e) {
			// 取消enQueue
			removeQueue(job);
			return Results.of(false, job, null, new ExceedExpectedJobEngineException(e));
		}
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
		boolean b = jobQueue.removeJob(job.getId());
		if (b) {
			metricsOverload.decrementOverload(job);
			metricsOverload.flushMetrics();
		}
		return b;
	}

	protected void runIfParallelSuccess(ExecutorInstanceLoadBalance executorInstanceLoadBalance, Job job,
			ParallelExchangeResult result) {
		/**
		 * 只要任一返回是true就需要callback
		 */
		boolean isOnParallelSuccessCallback = result.getShardExchangeResults().stream()
				.anyMatch(shardExchangeResult -> {
					ExecuteJobResult executeJobResult = (ExecuteJobResult) shardExchangeResult.response();
					return executeJobResult.isOnParallelSuccessCallback();
				});
		if (isOnParallelSuccessCallback) {
			CandidatesSwitchableLoadBalanceExchanger loadBalanceExchanger = new CandidatesSwitchableLoadBalanceExchanger(
					this.protocol, executorInstanceLoadBalance, NodeRole.Executor.getRoleName(),
					instanceProperties.getLoadBalance().getMaxCandidates());

			/*
			 * 设置有意义的参数
			 */
			job.setShardTotal(result.getShardTotal());

			RequestExecutorDTO dto = new RequestExecutorDTO(RequestExecutorDTO.METHOD_ONPARALLELSUCCESS, job);
			loadBalanceExchanger.exchange(dto, job.getExecuteTimeout());
		}
	}

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

	@Override
	public int queuedSize() {
		return jobQueue.queuedSize();
	}

	@Override
	public void shutdownBlocking(long blockTimeoutMillis) {
		jobQueue.shutdownBlocking(blockTimeoutMillis);
	}

	public abstract class JobTrigger implements Runnable {

		private final Long jobId;

		private long executedTimes;
		private boolean running = false;
		private ScheduledFuture<?> future;

		public JobTrigger(Long jobId) {
			this.jobId = jobId;
		}

		/**
		 * IMPT sync用于立即执行和调度触发的并发控制，这是每个job级别的不影响性能
		 */
		@Override
		public synchronized void run() {
			running = true;
			try {
				ExecutableJobBO job = jobService.findOneExecutableJob(jobId);
				if (job.getEnd()) {
					removeQueue(job);
					return;
				}

				doRun(job);
			} catch (Exception e) {
				// doRun 预期不会抛出异常，担保log
				log.error("ex on job run, expect no ex throw", e);
			} finally {
				running = false;
				executedTimes++;
			}
		}

		protected abstract void doRun(ExecutableJobBO job);

		public Long getJobId() {
			return jobId;
		}

		public long getExecutedTimes() {
			return executedTimes;
		}

		public boolean isRunning() {
			return running;
		}

		public ScheduledFuture<?> getFuture() {
			return future;
		}

		public void setFuture(ScheduledFuture<?> future) {
			this.future = future;
		}
	}
}

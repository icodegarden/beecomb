package io.github.icodegarden.beecomb.worker.core;

import java.time.LocalDateTime;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.executor.DelayJob;
import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.worker.exception.ExceedOverloadJobEngineException;
import io.github.icodegarden.beecomb.worker.exception.InvalidParamJobEngineException;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.beecomb.worker.loadbalance.ExecutorInstanceLoadBalance;
import io.github.icodegarden.beecomb.worker.registry.ExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.worker.registry.ExecutorRegisteredInstance;
import io.github.icodegarden.beecomb.worker.service.DelayJobStorage;
import io.github.icodegarden.beecomb.worker.service.JobStorage.UpdateOnExecuteFailed;
import io.github.icodegarden.beecomb.worker.service.JobStorage.UpdateOnExecuteSuccess;
import io.github.icodegarden.beecomb.worker.service.JobStorage.UpdateOnNoQualifiedExecutor;
import io.github.icodegarden.commons.exchange.CandidatesSwitchableLoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.ParallelExchanger;
import io.github.icodegarden.commons.exchange.ShardExchangeResult;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.exception.NoQualifiedInstanceExchangeException;
import io.github.icodegarden.commons.lang.concurrent.NamedThreadFactory;
import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.MetricsOverload;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
import io.github.icodegarden.commons.lang.result.Result1;
import io.github.icodegarden.commons.lang.result.Result2;
import io.github.icodegarden.commons.lang.result.Result3;
import io.github.icodegarden.commons.lang.result.Results;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class DelayJobEngine extends AbstractJobEngine {

	private ExecutorInstanceDiscovery executorInstanceDiscovery;
	private InstanceMetrics instanceMetrics;
	private DelayJobStorage delayJobStorage;

	private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	public DelayJobEngine(ExecutorInstanceDiscovery<? extends ExecutorRegisteredInstance> executorInstanceDiscovery,
			InstanceMetrics instanceMetrics, MetricsOverload jobOverload, DelayJobStorage delayJobStorage,
			InstanceProperties instanceProperties) {
		super(jobOverload, instanceProperties);
		
		this.executorInstanceDiscovery = executorInstanceDiscovery;
		this.instanceMetrics = instanceMetrics;
		this.delayJobStorage = delayJobStorage;
		
		this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(
				instanceProperties.getOverload().getJobs().getMax(), new NamedThreadFactory("delay-jobs"),
				new ThreadPoolExecutor.AbortPolicy());
		scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
	}
	
	@Override
	public String shutdownName() {
		return "delay";
	}

	@Override
	protected Result3<ExecutableJobBO, JobTrigger, JobEngineException> doEnQueue(ExecutableJobBO job) {
		DelayBO delay = job.getDelay();
		if (delay == null) {
			return Results.of(false, job, null, new InvalidParamJobEngineException("param delay must not null"));
		}
		long delayMillis = job.calcNextTrigDelayMillisOnEnQueue();
		return doEnQueue(job, delayMillis);
	}

	/**
	 * delay ms使用额外参数
	 * 
	 * @param job
	 * @param delayMillis
	 * @return
	 */
	private Result3<ExecutableJobBO, JobTrigger, JobEngineException> doEnQueue(ExecutableJobBO job, long delayMillis) {
		try {
			DelayJobTrigger delayJob = new DelayJobTrigger(job);
			ScheduledFuture<?> future = scheduledThreadPoolExecutor.schedule(delayJob, delayMillis,
					TimeUnit.MILLISECONDS);
			delayJob.setFuture(future);

			return Results.of(true, job, delayJob, null);
		} catch (RejectedExecutionException e) {
			return Results.of(false, job, null,
					new ExceedOverloadJobEngineException("Pool Rejected", metricsOverload.getLocalMetrics()));
		}
	}

	private class DelayJobTrigger extends JobTrigger {

		private final ExecutableJobBO job;

		public DelayJobTrigger(ExecutableJobBO job) {
			this.job = job;
		}

		@Override
		public void doRun() {
			DelayJobEngine.this.runJob(job);
		}
	}

	/**
	 * 
	 * @param job
	 * @param nioClientProvider Nullable,null时使用默认的
	 */
	void runJob(ExecutableJobBO executableJobBO) {
		LocalDateTime trigAt = SystemUtils.now();
		if (log.isInfoEnabled()) {
			log.info("run delay job:{}", executableJobBO);
		}

		try {
			UpdateOnExecuteSuccess update = exchange(executableJobBO);
			Result1<RuntimeException> result1 = delayJobStorage.updateOnExecuteSuccess(update);
			if (!result1.isSuccess()) {
				/**
				 * 如果最终还是失败，则该任务因为状态没更新，未来可能被再次触发，业务端最好能识别重复任务
				 */
				log.error("WARNING ex on update job", result1.getT1());
			}
			/**
			 * delay任务完成后减少度量
			 */
			metricsOverload.decrementOverload(executableJobBO);
			metricsOverload.flushMetrics();
		} catch (NoQualifiedInstanceExchangeException e) {
			if (log.isWarnEnabled()) {
				log.warn("No Qualified Executor on delay job run, job:{}, candidates:{}", executableJobBO,
						e.getCandidates());
			}

			UpdateOnNoQualifiedExecutor update = UpdateOnNoQualifiedExecutor.builder().jobId(executableJobBO.getId())
					.lastTrigAt(trigAt).noQualifiedInstanceExchangeException(e)
					.callback(jobFreshParamsCallback(executableJobBO)).build();
			Result2<Boolean, RuntimeException> result2 = delayJobStorage.updateOnNoQualifiedExecutor(update);
			if (!result2.isSuccess()) {
				log.error("WARNING ex on update job", result2.getT2());
			}
			long delayMillis = executableJobBO.getDelay().calcNextTrigDelayMillisOnNoQualified();
			queueOperAfterNotSuccess(executableJobBO, result2, delayMillis);
		} catch (ExchangeException e) {
			/**
			 * 其他ExchangeException都属于failed类型
			 */
			if (log.isWarnEnabled()) {
				log.warn("exchange failed on delay job run, job:{}, candidates:{}, exchangedInstances:{}",
						executableJobBO, e.getCandidates(), e.getExchangedInstances());
			}
			onFailed(executableJobBO, trigAt, e);
		} catch (Exception e) {
			log.error("ex on delay job run, job:{}", executableJobBO, e);
			onFailed(executableJobBO, trigAt, e);
		}
	}

	private UpdateOnExecuteSuccess exchange(ExecutableJobBO executableJobBO) {
		final String executorName = executableJobBO.getExecutorName();
		final String jobHandlerName = executableJobBO.getJobHandlerName();

		ExecutorInstanceLoadBalance executorInstanceLoadBalance = new ExecutorInstanceLoadBalance(
				executorInstanceDiscovery, instanceMetrics, executorName, jobHandlerName);

		if (executableJobBO.getParallel()) {
			DelayJob job = DelayJob.of(executableJobBO);

			ParallelExchanger.Config config = new ParallelExchanger.Config(
					instanceProperties.getLoadBalance().getMaxCandidates(), executableJobBO.getMaxParallelShards(),
					instanceProperties.getOverload().getJobs().getMax());
			/**
			 * 并行任务不关注返回体，只关注是否成功，所有分片全部成功才视为成功，否则会收到PartInstanceFailedExchangeException
			 */
			parallelLoadBalanceExchanger.exchange(job, executableJobBO.getExecuteTimeout(), executorInstanceLoadBalance,
					config);

			UpdateOnExecuteSuccess update = UpdateOnExecuteSuccess.builder().jobId(executableJobBO.getId())
					.executorIp("parallel").executorPort(0).lastExecuteReturns(null/* 并行任务不关注返回结果 */)
					.lastTrigAt(SystemUtils.now()).callback(jobFreshParamsCallback(executableJobBO)).build();
			
			return update;
		} else {
			CandidatesSwitchableLoadBalanceExchanger loadBalanceExchanger = new CandidatesSwitchableLoadBalanceExchanger(
					this.protocol, executorInstanceLoadBalance, NodeRole.Executor.getRoleName(),
					instanceProperties.getLoadBalance().getMaxCandidates());

			DelayJob job = DelayJob.of(executableJobBO);
			ShardExchangeResult result = loadBalanceExchanger.exchange(job, executableJobBO.getExecuteTimeout());
			ExecuteJobResult executeJobResult = (ExecuteJobResult) result.successResult().response();
			RegisteredInstance instance = result.successResult().instance().getAvailable();

			UpdateOnExecuteSuccess update = UpdateOnExecuteSuccess.builder().jobId(executableJobBO.getId())
					.executorIp(instance.getIp()).executorPort(instance.getPort())
					.lastExecuteReturns(executeJobResult.getExecuteReturns()).lastTrigAt(SystemUtils.now())
					.callback(jobFreshParamsCallback(executableJobBO)).build();
			
			return update;
		}
	}

	private void onFailed(ExecutableJobBO job, LocalDateTime trigAt, Exception e) {
		UpdateOnExecuteFailed update = UpdateOnExecuteFailed.builder().jobId(job.getId()).exception(e)
				.lastTrigAt(trigAt).callback(jobFreshParamsCallback(job)).build();
		Result2<Boolean, RuntimeException> result2 = delayJobStorage.updateOnExecuteFailed(update);

		if (!result2.isSuccess()) {
			log.error("WARNING ex on update job", result2.getT2());
		}
		long delayMillis = job.getDelay().calcNextTrigDelayMillisOnExecuteFailed();
		queueOperAfterNotSuccess(job, result2, delayMillis);
	}

	/**
	 * 决定是否重进队列，还是减少负载
	 * 
	 * @param job
	 * @param result2
	 * @param delayMillis
	 */
	private void queueOperAfterNotSuccess(ExecutableJobBO job, Result2<Boolean, RuntimeException> result2,
			long delayMillis) {
		if (!result2.getT1()) {
			/**
			 * 没到阈值,重进队列
			 */
			Result3<ExecutableJobBO, JobTrigger, JobEngineException> result3 = reEnQueue(job, delayMillis);
			if (!result3.isSuccess()) {
				/**
				 * 如果失败则等待任务的恢复机制
				 */
				JobEngineException exception = result3.getT3();
				log.warn("job reEnQueue not success, reason:{}, job:{}", exception.getReason(), job);
			}
		} else {
			/**
			 * 到阈值时，需要输出error log
			 */
			log.error("WARNING delay job is threshold of failed times, the job is end, job:{}", job, result2.getT2());

			/**
			 * delay任务到阈值后减少度量
			 */
			metricsOverload.decrementOverload(job);
			metricsOverload.flushMetrics();
		}
	}

	private Result3<ExecutableJobBO, JobTrigger, JobEngineException> reEnQueue(ExecutableJobBO job, long delayMillis) {
		if (log.isInfoEnabled()) {
			log.info("delay job reEnQueue with delayMillis:{}", delayMillis);
		}
		return doEnQueue(job, delayMillis);
	}

	/**
	 * 从队列中移除任务并减少相应的负载<br>
	 * ScheduledFuture 的cancel只有在 已经完成 或 已经取消 的状态下才会false，进行中的任务也能true<br>
	 * 关于 scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
	 * 默认false，任务只有在完成时才从队列移除，cancel是不会触发移除的（要等到任务触发时间到了才真正从队列remove）<br>
	 */
	@Override
	public boolean removeQueue(Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueueResult) {
		metricsOverload.decrementOverload(enQueueResult.getT1());

		JobTrigger jobTrigger = enQueueResult.getT2();
		ScheduledFuture<?> future = jobTrigger.getFuture();
		if (!future.isDone() && !future.isCancelled()) {
			return future.cancel(true);
		}
		return true;
	}

	/**
	 * delay类型的任务，在处于执行中时，将从队列中移除，此时队列的size不会包含该任务
	 */
	@Override
	public int queuedSize() {
		return scheduledThreadPoolExecutor.getQueue().size();
	}
	
	@Override
	public void shutdownBlocking(long blockTimeoutMillis) {
		scheduledThreadPoolExecutor.shutdown();
		try {
			scheduledThreadPoolExecutor.awaitTermination(blockTimeoutMillis, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ignore) {
		}
	}
}

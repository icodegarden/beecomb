package io.github.icodegarden.beecomb.worker.core;

import java.time.LocalDateTime;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.icodegarden.beecomb.common.backend.executor.registry.ExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.common.backend.executor.registry.ExecutorRegisteredInstance;
import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.executor.DelayJob;
import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.transfer.RequestExecutorDTO;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.worker.exception.ExceedOverloadJobEngineException;
import io.github.icodegarden.beecomb.worker.exception.InvalidParamJobEngineException;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.beecomb.worker.loadbalance.ExecutorInstanceLoadBalance;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteFailedDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteSuccessDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnNoQualifiedExecutorDTO;
import io.github.icodegarden.beecomb.worker.service.DelayJobService;
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
	private DelayJobService delayJobService;

	public DelayJobEngine(ExecutorInstanceDiscovery<? extends ExecutorRegisteredInstance> executorInstanceDiscovery,
			InstanceMetrics instanceMetrics, MetricsOverload jobOverload, DelayJobService delayJobService,
			InstanceProperties instanceProperties) {
		super(delayJobService, jobOverload, instanceProperties, buildJobQueue(instanceProperties));

		this.executorInstanceDiscovery = executorInstanceDiscovery;
		this.instanceMetrics = instanceMetrics;
		this.delayJobService = delayJobService;
	}

	private static JobQueue buildJobQueue(InstanceProperties instanceProperties) {
		ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(
				instanceProperties.getOverload().getJobs().getMax(), new NamedThreadFactory("delay-jobs"),
				new ThreadPoolExecutor.AbortPolicy());
		scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);

		return new JobQueue(scheduledThreadPoolExecutor);
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
	 * delay ms??????????????????
	 * 
	 * @param job
	 * @param delayMillis
	 * @return
	 */
	private Result3<ExecutableJobBO, JobTrigger, JobEngineException> doEnQueue(ExecutableJobBO job, long delayMillis) {
		try {
			DelayJobTrigger trigger = new DelayJobTrigger(job.getId());
			jobQueue.schedule(trigger, delayMillis, TimeUnit.MILLISECONDS);

			return Results.of(true, job, trigger, null);
		} catch (RejectedExecutionException e) {
			return Results.of(false, job, null,
					new ExceedOverloadJobEngineException("Pool Rejected", metricsOverload.getLocalMetrics()));
		}
	}

	private class DelayJobTrigger extends JobTrigger {

		public DelayJobTrigger(Long jobId) {
			super(jobId);
		}

		@Override
		public void doRun(ExecutableJobBO job) {
			/**
			 * delay????????????????????????????????????scheduledThreadPoolExecutor.queue???????????????????????????????????????????????????????????????queue?????????????????????????????????remove
			 */
			jobQueue.removeJobTrigger(job.getId());

			DelayJobEngine.this.runJob(job);
		}
	}

	void runJob(ExecutableJobBO executableJobBO) {
		LocalDateTime trigAt = SystemUtils.now();
		if (log.isInfoEnabled()) {
			log.info("run delay job:{}", executableJobBO);
		}

		try {
			UpdateOnExecuteSuccessDTO update = exchange(executableJobBO);
			Result1<RuntimeException> result1 = delayJobService.updateOnExecuteSuccess(update);
			if (!result1.isSuccess()) {
				/**
				 * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
				 */
				log.error("WARNING ex on update job", result1.getT1());
			}
			/**
			 * delay???????????????????????????
			 */
			metricsOverload.decrementOverload(executableJobBO);
			metricsOverload.flushMetrics();
		} catch (NoQualifiedInstanceExchangeException e) {
			if (log.isWarnEnabled()) {
				log.warn("No Qualified Executor on delay job run, job:{}, candidates:{}", executableJobBO,
						e.getCandidates());
			}

			UpdateOnNoQualifiedExecutorDTO update = UpdateOnNoQualifiedExecutorDTO.builder()
					.jobId(executableJobBO.getId()).lastTrigAt(trigAt).noQualifiedInstanceExchangeException(e).build();
			Result2<Boolean, RuntimeException> result2 = delayJobService.updateOnNoQualifiedExecutor(update);
			if (!result2.isSuccess()) {
				log.error("WARNING ex on update job", result2.getT2());
			}
			long delayMillis = executableJobBO.getDelay().calcNextTrigDelayMillisOnNoQualified();
			queueOperAfterNotSuccess(executableJobBO, result2, delayMillis);
		} catch (ExchangeException e) {
			/**
			 * ??????ExchangeException?????????failed??????
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

	private UpdateOnExecuteSuccessDTO exchange(ExecutableJobBO executableJobBO) throws ExchangeException {
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
			 * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????PartInstanceFailedExchangeException
			 */
			RequestExecutorDTO dto = new RequestExecutorDTO(RequestExecutorDTO.METHOD_RECEIVEJOB, job);
			parallelLoadBalanceExchanger.exchange(dto, executableJobBO.getExecuteTimeout(), executorInstanceLoadBalance,
					config);

			UpdateOnExecuteSuccessDTO update = UpdateOnExecuteSuccessDTO.builder().jobId(executableJobBO.getId())
					.executorIp("parallel").executorPort(0).lastExecuteReturns(null/* ????????????????????????????????? */)
					.lastTrigAt(SystemUtils.now()).build();

			return update;
		} else {
			CandidatesSwitchableLoadBalanceExchanger loadBalanceExchanger = new CandidatesSwitchableLoadBalanceExchanger(
					this.protocol, executorInstanceLoadBalance, NodeRole.Executor.getRoleName(),
					instanceProperties.getLoadBalance().getMaxCandidates());

			DelayJob job = DelayJob.of(executableJobBO);

			RequestExecutorDTO dto = new RequestExecutorDTO(RequestExecutorDTO.METHOD_RECEIVEJOB, job);
			ShardExchangeResult result = loadBalanceExchanger.exchange(dto, executableJobBO.getExecuteTimeout());

			ExecuteJobResult executeJobResult = (ExecuteJobResult) result.successResult().response();
			RegisteredInstance instance = result.successResult().instance().getAvailable();

			UpdateOnExecuteSuccessDTO update = UpdateOnExecuteSuccessDTO.builder().jobId(executableJobBO.getId())
					.executorIp(instance.getIp()).executorPort(instance.getPort())
					.lastExecuteReturns(executeJobResult.getExecuteReturns()).lastTrigAt(SystemUtils.now()).build();

			return update;
		}
	}

	private void onFailed(ExecutableJobBO job, LocalDateTime trigAt, Exception e) {
		UpdateOnExecuteFailedDTO update = UpdateOnExecuteFailedDTO.builder().jobId(job.getId()).exception(e)
				.lastTrigAt(trigAt).build();
		Result2<Boolean, RuntimeException> result2 = delayJobService.updateOnExecuteFailed(update);

		if (!result2.isSuccess()) {
			log.error("WARNING ex on update job", result2.getT2());
		}
		long delayMillis = job.getDelay().calcNextTrigDelayMillisOnExecuteFailed();
		queueOperAfterNotSuccess(job, result2, delayMillis);
	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param job
	 * @param result2
	 * @param delayMillis
	 */
	private void queueOperAfterNotSuccess(ExecutableJobBO job, Result2<Boolean, RuntimeException> result2,
			long delayMillis) {
		if (!result2.getT1()) {
			/**
			 * ????????????,????????????
			 */
			Result3<ExecutableJobBO, JobTrigger, JobEngineException> result3 = reEnQueue(job, delayMillis);
			if (!result3.isSuccess()) {
				/**
				 * ??????????????????????????????????????????
				 */
				JobEngineException exception = result3.getT3();
				log.warn("job reEnQueue not success, reason:{}, job:{}", exception.getReason(), job);
			}
		} else {
			/**
			 * ???????????????????????????error log
			 */
			log.error("WARNING delay job is threshold of failed times, the job is end, job:{}", job, result2.getT2());

			/**
			 * delay??????????????????????????????
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
}

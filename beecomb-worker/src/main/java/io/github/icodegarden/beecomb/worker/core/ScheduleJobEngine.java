package io.github.icodegarden.beecomb.worker.core;

import java.time.LocalDateTime;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.icodegarden.beecomb.common.backend.executor.registry.ExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.ScheduleJob;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ScheduleBO;
import io.github.icodegarden.beecomb.common.pojo.transfer.RequestExecutorDTO;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.worker.exception.ExceedOverloadJobEngineException;
import io.github.icodegarden.beecomb.worker.exception.InvalidParamJobEngineException;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.beecomb.worker.loadbalance.ExecutorInstanceLoadBalance;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteFailedDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteSuccessDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnNoQualifiedExecutorDTO;
import io.github.icodegarden.beecomb.worker.service.ScheduleJobService;
import io.github.icodegarden.commons.exchange.CandidatesSwitchableLoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.ParallelExchangeResult;
import io.github.icodegarden.commons.exchange.ParallelExchanger;
import io.github.icodegarden.commons.exchange.ShardExchangeResult;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.exception.NoQualifiedInstanceExchangeException;
import io.github.icodegarden.commons.lang.concurrent.NamedThreadFactory;
import io.github.icodegarden.commons.lang.metricsregistry.InstanceMetrics;
import io.github.icodegarden.commons.lang.metricsregistry.MetricsOverload;
import io.github.icodegarden.commons.lang.metricsregistry.RegisteredInstance;
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
public class ScheduleJobEngine extends AbstractJobEngine {

	private ExecutorInstanceDiscovery executorInstanceDiscovery;
	private InstanceMetrics instanceMetrics;
	private ScheduleJobService scheduleJobService;

	public ScheduleJobEngine(ExecutorInstanceDiscovery executorInstanceDiscovery, InstanceMetrics instanceMetrics,
			MetricsOverload jobOverload, ScheduleJobService scheduleJobService, InstanceProperties instanceProperties) {
		super(scheduleJobService, jobOverload, instanceProperties, buildJobQueue(instanceProperties));
		this.executorInstanceDiscovery = executorInstanceDiscovery;
		this.instanceMetrics = instanceMetrics;
		this.scheduleJobService = scheduleJobService;
	}

	private static JobQueue buildJobQueue(InstanceProperties instanceProperties) {
		ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(
				instanceProperties.getOverload().getJobs().getMax(), new NamedThreadFactory("schedule-jobs"),
				new ThreadPoolExecutor.AbortPolicy());
		scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);

		return new JobQueue(scheduledThreadPoolExecutor);
	}

	@Override
	public String shutdownName() {
		return "schedule";
	}

	@Override
	protected Result3<ExecutableJobBO, JobTrigger, JobEngineException> doEnQueue(ExecutableJobBO job) {
		ScheduleBO schedule = job.getSchedule();
		if (schedule == null) {
			return Results.of(false, job, null, new InvalidParamJobEngineException("param schedule must not null"));
		}
		try {
			ScheduleJobTrigger trigger = new ScheduleJobTrigger(job.getId());

			/**
			 * 任务的首次执行延迟
			 */
			long nextDelayMillis = schedule.calcNextTrigDelayMillisOnEnQueue();

			if (schedule.getScheduleFixDelay() != null) {
				jobQueue.scheduleWithFixedDelay(trigger, nextDelayMillis, schedule.getScheduleFixDelay(),
						TimeUnit.MILLISECONDS);
			} else if (schedule.getScheduleFixRate() != null) {
				jobQueue.scheduleAtFixedRate(trigger, nextDelayMillis, schedule.getScheduleFixRate(),
						TimeUnit.MILLISECONDS);
			} else {
				// 计算出下次执行时间
				jobQueue.schedule(trigger, nextDelayMillis, TimeUnit.MILLISECONDS);
			}

			return Results.of(true, job, trigger, null);
		} catch (RejectedExecutionException e) {
			return Results.of(false, job, null,
					new ExceedOverloadJobEngineException("Pool Rejected", metricsOverload.getLocalMetrics()));
		}
	}

	@Override
	public boolean run(ExecutableJobBO job) {
		JobTrigger jobTrigger = jobQueue.getJobTrigger(job.getId());
		if (jobTrigger == null) {
			return false;
		}
		try {
			jobTrigger.run();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private class ScheduleJobTrigger extends JobTrigger {

		public ScheduleJobTrigger(Long jobId) {
			super(jobId);
		}

		@Override
		public void doRun(ExecutableJobBO job) {
			boolean end = ScheduleJobEngine.this.runJob(job);
			if (end) {
				removeQueue(job);
			} else {
				reEnQueueIfCron(job);
			}
		}

		/**
		 * 如果是cron的，则需要重进队列
		 */
		private void reEnQueueIfCron(ExecutableJobBO job) {
			if (job.getSchedule().getSheduleCron() != null) {
				/**
				 * 原因同delay
				 */
				jobQueue.removeJobTrigger(job.getId());

				Result3<ExecutableJobBO, JobTrigger, JobEngineException> result3 = doEnQueue(job);// 以新的身份重进队列
				if (result3.isSuccess()) {
					this.setFuture(result3.getT2().getFuture());
				} else {
					if (log.isWarnEnabled()) {
						log.warn("schedule job with cron reEnQueue failed after run, job:{}", result3.getT1(),
								result3.getT3());
					}
					// 失败则通过恢复机制
				}
			}
		}
	}

	/**
	 * 
	 * @param job
	 * @param nioClientProvider Nullable,null时使用默认的
	 * @return 是否end
	 */
	boolean runJob(ExecutableJobBO executableJobBO) {
		LocalDateTime trigAt = SystemUtils.now();
		if (log.isInfoEnabled()) {
			log.info("run schedule job:{}", executableJobBO);
		}

		try {
			UpdateOnExecuteSuccessDTO update = exchange(executableJobBO, trigAt);
			Result1<RuntimeException> result1 = scheduleJobService.updateOnExecuteSuccess(update);

			if (!result1.isSuccess()) {
				/**
				 * 如果最终还是失败，则该任务因为状态没更新，未来可能被再次触发，业务端最好能识别重复任务
				 */
				log.error("WARNING ex on update job", result1.getT1());
			}
			return update.getEnd();
		} catch (NoQualifiedInstanceExchangeException e) {
			if (log.isWarnEnabled()) {
				log.warn("No Qualified Executor on schedule job run, job:{}, candidates:{}", executableJobBO,
						e.getCandidates());
			}
			LocalDateTime nextTrigAt = executableJobBO.getSchedule().calcNextTrigAtOnTriggered(trigAt,
					SystemUtils.now());
			UpdateOnNoQualifiedExecutorDTO update = UpdateOnNoQualifiedExecutorDTO.builder()//
					.jobId(executableJobBO.getId())//
					.noQualifiedInstanceExchangeException(e)//
					.lastTrigAt(trigAt)//
					.nextTrigAt(nextTrigAt)//
					.build();
			Result2<Boolean, RuntimeException> result2 = scheduleJobService.updateOnNoQualifiedExecutor(update);
			if (!result2.isSuccess()) {
				log.error("WARNING ex on update job", result2.getT2());
			}
		} catch (ExchangeException e) {
			/**
			 * 其他ExchangeException都属于failed类型
			 */
			if (log.isWarnEnabled()) {
				log.warn("exchange failed on schedule job run, job:{}, candidates:{}, exchangedInstances:{}",
						executableJobBO, e.getCandidates(), e.getExchangedInstances());
			}
			onFailed(executableJobBO, trigAt, e);
		} catch (Exception e) {
			log.error("ex on schedule job run, job:{}", executableJobBO, e);
			onFailed(executableJobBO, trigAt, e);
		}

		return false;
	}

	private UpdateOnExecuteSuccessDTO exchange(ExecutableJobBO executableJobBO, LocalDateTime trigAt) {
		final String executorName = executableJobBO.getExecutorName();
		final String jobHandlerName = executableJobBO.getJobHandlerName();

		ExecutorInstanceLoadBalance executorInstanceLoadBalance = new ExecutorInstanceLoadBalance(
				executorInstanceDiscovery, instanceMetrics, executorName, jobHandlerName);

		ScheduleJob job = ScheduleJob.of(executableJobBO);
		if (executableJobBO.getParallel()) {
			ParallelExchanger.Config config = new ParallelExchanger.Config(
					instanceProperties.getLoadBalance().getMaxCandidates(), executableJobBO.getMaxParallelShards(),
					instanceProperties.getOverload().getJobs().getMax());
			/**
			 * schedule类型需要关注并行结果
			 */
			RequestExecutorDTO dto = new RequestExecutorDTO(RequestExecutorDTO.METHOD_RECEIVEJOB, job);
			ParallelExchangeResult result = parallelLoadBalanceExchanger.exchange(dto,
					executableJobBO.getExecuteTimeout(), executorInstanceLoadBalance, config);

			runIfParallelSuccess(executorInstanceLoadBalance, job, result);

			/**
			 * 所有分片结果一致都是end则才真的end<br>
			 * 所有分片全部成功才视为成功，否则会收到PartInstanceFailedExchangeException
			 */
			boolean end = result.getShardExchangeResults().stream().allMatch(shardExchangeResult -> {
				ExecuteJobResult executeJobResult = (ExecuteJobResult) shardExchangeResult.response();
				return executeJobResult.isEnd();
			});

			LocalDateTime nextTrigAt = executableJobBO.getSchedule().calcNextTrigAtOnTriggered(trigAt,
					SystemUtils.now());
			UpdateOnExecuteSuccessDTO update = UpdateOnExecuteSuccessDTO.builder()//
					.jobId(executableJobBO.getId())//
					.executorIp("parallel")//
					.executorPort(0)//
					.lastExecuteReturns(null/* 并行任务不关注返回结果 */)//
					.end(end)//
					.lastTrigAt(trigAt)//
					.nextTrigAt(nextTrigAt)//
					.build();
			return update;
		} else {
			CandidatesSwitchableLoadBalanceExchanger loadBalanceExchanger = new CandidatesSwitchableLoadBalanceExchanger(
					this.protocol, executorInstanceLoadBalance, NodeRole.Executor.getRoleName(),
					instanceProperties.getLoadBalance().getMaxCandidates());

			RequestExecutorDTO dto = new RequestExecutorDTO(RequestExecutorDTO.METHOD_RECEIVEJOB, job);
			ShardExchangeResult result = loadBalanceExchanger.exchange(dto, executableJobBO.getExecuteTimeout());

			ExecuteJobResult executeJobResult = (ExecuteJobResult) result.successResult().response();
			RegisteredInstance instance = result.successResult().instance().getAvailable();

			LocalDateTime nextTrigAt = executableJobBO.getSchedule().calcNextTrigAtOnTriggered(trigAt,
					SystemUtils.now());
			UpdateOnExecuteSuccessDTO update = UpdateOnExecuteSuccessDTO.builder()//
					.jobId(executableJobBO.getId())//
					.executorIp(instance.getIp())//
					.executorPort(instance.getPort())//
					.lastExecuteReturns(executeJobResult.getExecuteReturns())//
					.end(executeJobResult.isEnd())//
					.lastTrigAt(trigAt)//
					.nextTrigAt(nextTrigAt)//
					.build();
			return update;
		}
	}

	private void onFailed(ExecutableJobBO job, LocalDateTime trigAt, Exception e) {
		LocalDateTime nextTrigAt = job.getSchedule().calcNextTrigAtOnTriggered(trigAt, SystemUtils.now());
		UpdateOnExecuteFailedDTO update = UpdateOnExecuteFailedDTO.builder()//
				.jobId(job.getId())//
				.exception(e)//
				.lastTrigAt(trigAt)//
				.nextTrigAt(nextTrigAt)//
				.build();
		Result2<Boolean, RuntimeException> result2 = scheduleJobService.updateOnExecuteFailed(update);

		if (!result2.isSuccess()) {
			log.error("WARNING ex on update job", result2.getT2());
		}
	}

}

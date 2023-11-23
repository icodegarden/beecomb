package io.github.icodegarden.beecomb.master.schedule;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import io.github.icodegarden.beecomb.common.constant.JobConstants;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.manager.JobRecoveryRecordManager;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateOrUpdateJobRecoveryRecordDTO;
import io.github.icodegarden.beecomb.master.service.JobFacadeManager;
import io.github.icodegarden.beecomb.master.service.WorkerRemoteService;
import io.github.icodegarden.nutrient.exchange.exception.ExchangeException;
import io.github.icodegarden.nutrient.exchange.exception.NoSwitchableExchangeException;
import io.github.icodegarden.nutrient.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.nutrient.lang.schedule.LockSupportSchedule;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 自动重置任务未队列（作为JobRecoveryListener的补充）<br>
 * 自动恢复任务的调度
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class JobRecoverySchedule extends LockSupportSchedule {

	private final JobFacadeManager jobFacadeManager;
	private final WorkerRemoteService remoteService;
	private final JobRecoveryRecordManager jobRecoveryRecordService;
	
	private static final int TIMEOUT_MINUS = 60 * 1000;

	public JobRecoverySchedule(DistributedLock lock, JobFacadeManager jobFacadeManager,
			WorkerRemoteService remoteService, JobRecoveryRecordManager jobRecoveryRecordService) {
		super(lock);
		this.jobFacadeManager = jobFacadeManager;
		this.remoteService = remoteService;
		this.jobRecoveryRecordService = jobRecoveryRecordService;
	}

	@Override
	protected void doScheduleAfterLocked() throws Throwable {
		doRecovery();

		/**
		 * 需要优化?当前recoveryThatNoQueuedActually 和 listJobsShouldRecovery处于相同的schedule中
		 */
		doDispatch();
	}

	private void doRecovery() {
		/**
		 * 原来使用nextTrigAtLt=最大执行超时时间+60s的任务
		 * nextTrigAt没有得到更新则视为不在队列中，优点是检测准确度高，缺点是执行频率高的任务不能及时重新进队列<br>
		 * 现在使用nextTrigAtLt=60s的任务
		 * nextTrigAt没有得到更新则视为不在队列中，优点是任务能及时重新进队列，缺点是如果任务的超时时间超过60s则会误判，因此这样的任务在重新进队列前会加一次是否真正在队列中的检测<br>
		 */
//		LocalDateTime nextTrigAtLt = SystemUtils.now().minus(JobConstants.MAX_EXECUTE_TIMEOUT + TIMEOUT_MINUS,
//				ChronoUnit.MILLIS);
		LocalDateTime nextTrigAtLt = SystemUtils.now().minus(TIMEOUT_MINUS, ChronoUnit.MILLIS);
		/**
		 * 探测，可能节省不必要的开支
		 */
		boolean has = jobFacadeManager.hasNoQueuedActually(nextTrigAtLt);
		if (log.isInfoEnabled()) {
			log.info("recovery jobs nextTrigAtLt:{}, Has No Queued Actually:{}", nextTrigAtLt, has);
		}
		if (has) {
			int count = jobFacadeManager.recoveryThatNoQueuedActually(nextTrigAtLt);
			if (log.isInfoEnabled()) {
				log.info("recovery jobs nextTrigAtLt:{} count:{}", nextTrigAtLt, count);
			}
		}
	}

	private void doDispatch() {
		int skip = 0;
		for (;;) {
			if (isClosed()) {
				/**
				 * 如果在执行过程中关闭，终止执行
				 */
				return;
			}

			if (!lock.isAcquired()) {
				log.warn("lock was not Acquired, stop list Jobs Should Recovery");
				/**
				 * 每轮检查锁是否还持有，因为zk的锁可能由于网络问题session超时而失去持有被其他进程获取锁
				 */
				return;
			}
			List<ExecutableJobBO> jobs = jobFacadeManager.listJobsShouldRecovery(skip, 10);
			if (log.isInfoEnabled() && !jobs.isEmpty()) {
				log.info("list Jobs Should Recovery, skip:{}, resultSize:{}", skip, jobs.size());
			}
			if (jobs.isEmpty()) {
				return;
			}
			for (ExecutableJobBO job : jobs) {
				if(job.getExecuteTimeout() >= TIMEOUT_MINUS) {
					/**
					 * 如果任务的超过时间>=60s，则doRecovery阶段对该任务进到pending_recovery_job可能会误判，需要确认是否真的不在队列中
					 */
					boolean isQueued = remoteService.isQueued(job);
					if(isQueued) {
						skip++;//下一轮要跳过这个
						continue;
					}
				}
				
				CreateOrUpdateJobRecoveryRecordDTO dto = new CreateOrUpdateJobRecoveryRecordDTO();
				dto.setJobId(job.getId());
				dto.setRecoveryAt(SystemUtils.now());
				try {
					remoteService.enQueue(job);

					dto.setSuccess(true);
					dto.setDesc("");
				} catch (NoSwitchableExchangeException e) {
					log.warn("exchange failed on dispatch job on JobRecovery", e);
					dto.setSuccess(false);
					dto.setDesc(e.getMessage());

					/**
					 * 当出现NoSwitchableExchangeException时说明还可以尝试下一波的任务,
					 * 因为这个异常是RequestTimeout或ServerException
					 */

					/**
					 * 所以后几波的查询都需要跳过这个任务，以免陷入循环
					 */
					skip++;
				} catch (ExchangeException e) {
					log.warn("exchange failed on dispatch job on JobRecovery", e);
					dto.setSuccess(false);
					dto.setDesc(e.getMessage());

					/**
					 * 其他的ExchangeException时说明没有worker可以接收，可以终止了
					 */
					return;// IMPT
				} finally {
					jobRecoveryRecordService.createOrUpdate(dto);
				}
			}
		}
	}

}

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
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.exception.NoSwitchableExchangeException;
import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.schedule.LockSupportSchedule;
import io.github.icodegarden.commons.lang.util.SystemUtils;
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
		LocalDateTime nextTrigAtLt = SystemUtils.now().minus(JobConstants.MAX_EXECUTE_TIMEOUT + 60 * 1000,
				ChronoUnit.MILLIS);
		if (log.isInfoEnabled()) {
			log.info("recovery jobs nextTrigAtLt:{}", nextTrigAtLt);
		}
		/**
		 * 探测，可能节省不必要的开支
		 */
		boolean has = jobFacadeManager.hasNoQueuedActually(nextTrigAtLt);
		if (log.isInfoEnabled()) {
			log.info("recovery jobs Has No Queued Actually:{}", has);
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
			if (log.isInfoEnabled()) {
				log.info("list Jobs Should Recovery size:{}", jobs.size());
			}
			if (jobs.isEmpty()) {
				return;
			}
			for (ExecutableJobBO job : jobs) {
				CreateOrUpdateJobRecoveryRecordDTO dto = new CreateOrUpdateJobRecoveryRecordDTO();
				dto.setJobId(job.getId());
				dto.setRecoveryAt(SystemUtils.now());
				try {
					remoteService.enQueue(job);
					
					try {
						Thread.sleep(100);//FIXME 避免有大批的任务需要恢复时，Executor被瞬间压力？ 
					} catch (InterruptedException e) {
					}

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

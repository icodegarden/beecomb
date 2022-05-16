package io.github.icodegarden.beecomb.master.schedule;

import java.io.Closeable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.icodegarden.beecomb.common.constant.JobConstants;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.manager.JobRecoveryRecordManager;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateOrUpdateJobRecoveryRecordDTO;
import io.github.icodegarden.beecomb.master.service.JobDispatcher;
import io.github.icodegarden.beecomb.master.service.JobService;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.exception.NoSwitchableExchangeException;
import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.lang.util.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class JobRecoverySchedule implements Closeable {

	private final ScheduledThreadPoolExecutor scheduleRecoveryThreadPool = ThreadPoolUtils
			.newSingleScheduledThreadPool("JobRecovery-recovery");
	{
		scheduleRecoveryThreadPool.setRemoveOnCancelPolicy(true);
	}

	private final AtomicBoolean closed = new AtomicBoolean(true);
	private final DistributedLock lock;
	private final JobService jobStorage;
	private final JobDispatcher jobDispatcher;
	private final JobRecoveryRecordManager jobRecoveryRecordService;

	private ScheduledFuture<?> future;

	public JobRecoverySchedule(DistributedLock lock, JobService jobStorage, JobDispatcher jobDispatcher,
			JobRecoveryRecordManager jobRecoveryRecordService) {
		this.lock = lock;
		this.jobStorage = jobStorage;
		this.jobDispatcher = jobDispatcher;
		this.jobRecoveryRecordService = jobRecoveryRecordService;
	}

	public boolean start(long scheduleMillis) {
		if (closed.compareAndSet(true, false)) {
			future = scheduleRecoveryThreadPool.scheduleWithFixedDelay(() -> {
				synchronized (JobRecoverySchedule.this) {
					if (closed.get()) {
						/**
						 * 如果已关闭，终止执行
						 */
						return;
					}
					try {
						if (lock.acquire(1000)) {
							try {
								doRecovery();

								/**
								 * FIXME 需要优化？当前recoveryThatNoQueuedActually 和
								 * listJobsShouldRecovery处于相同的schedule中
								 */
								doDispatch();
							} finally {
								lock.release();
							}
						}
					} catch (Throwable e) {
						log.error("WARNING: ex on recovery jobs", e);
					}
				}
			}, scheduleMillis, scheduleMillis, TimeUnit.MILLISECONDS);

			return true;
		}
		return false;
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
		boolean has = jobStorage.hasNoQueuedActually(nextTrigAtLt);
		if (log.isInfoEnabled()) {
			log.info("recovery jobs Has No Queued Actually:{}", has);
		}
		if (has) {
			jobStorage.recoveryThatNoQueuedActually(nextTrigAtLt);
		}
	}

	private void doDispatch() {
		int skip = 0;
		for (;;) {
			if (closed.get()) {
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
			List<ExecutableJobBO> jobs = jobStorage.listJobsShouldRecovery(skip, 10);
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
					jobDispatcher.dispatch(job);

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

	/**
	 * 阻塞直到处理完毕，这不会阻塞很久
	 */
	@Override
	public void close() {
		if (future != null) {
			future.cancel(true);
		}
		closed.set(true);

		/**
		 * 使用synchronized保障如果任务正在处理中，则等待任务处理完毕
		 */
		synchronized (this) {
		}
	}

}

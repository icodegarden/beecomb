package io.github.icodegarden.beecomb.worker.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.icodegarden.beecomb.worker.core.AbstractJobEngine.JobTrigger;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class JobQueue {

	private final Map<Long/* jobId */, JobTrigger> queuedJobs = new ConcurrentHashMap<Long, JobTrigger>(64);

	private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	public JobQueue(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
		this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
	}

	public ScheduledFuture<?> schedule(JobTrigger trigger, long delay, TimeUnit unit) {
		ScheduledFuture<?> future = scheduledThreadPoolExecutor.schedule(trigger, delay, unit);
		trigger.setFuture(future);

		queuedJobs.put(trigger.getJobId(), trigger);
		return future;
	}

//	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
//		return scheduledThreadPoolExecutor.schedule(callable, delay, unit);
//	}

	public ScheduledFuture<?> scheduleAtFixedRate(JobTrigger trigger, long initialDelay, long period, TimeUnit unit) {
		ScheduledFuture<?> future = scheduledThreadPoolExecutor.scheduleAtFixedRate(trigger, initialDelay, period,
				unit);
		trigger.setFuture(future);
		queuedJobs.put(trigger.getJobId(), trigger);
		return future;
	}

	public ScheduledFuture<?> scheduleWithFixedDelay(JobTrigger trigger, long initialDelay, long delay, TimeUnit unit) {
		ScheduledFuture<?> future = scheduledThreadPoolExecutor.scheduleWithFixedDelay(trigger, initialDelay, delay, unit);
		trigger.setFuture(future);
		queuedJobs.put(trigger.getJobId(), trigger);
		return future;
	}

	public boolean removeJob(Long jobId) {
		JobTrigger jobTrigger = queuedJobs.remove(jobId);
		if (jobTrigger == null) {
			if (log.isInfoEnabled()) {
				log.info("removeQueue job not found, jobId:{}", jobId);
			}
			/**
			 * 已不存在
			 */
			return true;
		}

		boolean b = cancelJob(jobTrigger);
		if (log.isInfoEnabled()) {
			log.info("removeQueue result:{}, jobId:{}", b, jobId);
		}
		return b;
	}

	/**
	 * Delay:cancel只有在 已经完成 或 已经取消 的状态下才会false，进行中的任务也能true<br>
	 * Sheducle:进行中的任务cancel也能true（进行中的任务cancel后下次不会再进入队列，当然不会再触发调度）<br>
	 * 关于 scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
	 * 默认false，任务只有在完成时才从队列移除，cancel是不会触发移除的（要等到任务触发时间到了才真正从队列remove）<br>
	 */
	private boolean cancelJob(JobTrigger jobTrigger) {
		ScheduledFuture<?> future = jobTrigger.getFuture();
		if (!future.isDone() && !future.isCancelled()) {
			/**
			 * IMPT 这里cancel(false)不要使用true，一方面没必要，另一方面因为remove
			 * queue后后续要把度量刷入zk，那时zk会报InterruptedException导致无法刷入
			 * （因为这里true的话会中断线程，即对应的本线程，而zk的sdk会进行object.wait而报InterruptedException）
			 */
			return future.cancel(false);
		}
		return true;
	}
	
//	public boolean containsJob(Long jobId) {
//		return queuedJobs.containsKey(jobId);
//	}

	public JobTrigger getJobTrigger(Long jobId) {
		return queuedJobs.get(jobId);
	}

	public void removeJobTrigger(Long jobId) {
		queuedJobs.remove(jobId);
	}

	/**
	 * delay类型的任务，在处于执行中时，将从队列中移除，此时队列的size不会包含该任务<br>
	 * schedule类型的任务，在处于执行中时，将从队列中暂时移除，此时队列的size不会包含该任务，直到任务执行完毕再加入到队列中，此时size包含该任务
	 */
	public int queuedSize() {
		return scheduledThreadPoolExecutor.getQueue().size();
	}

	public void shutdownBlocking(long blockTimeoutMillis) {
		scheduledThreadPoolExecutor.shutdown();
		try {
			scheduledThreadPoolExecutor.awaitTermination(blockTimeoutMillis, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ignore) {
		}
	}
}

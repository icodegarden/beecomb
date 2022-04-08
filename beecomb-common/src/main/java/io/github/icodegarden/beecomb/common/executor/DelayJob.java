package io.github.icodegarden.beecomb.common.executor;

import java.time.LocalDateTime;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DelayJob extends Job {
	private static final long serialVersionUID = -8461519222526601867L;

	/**
	 * millis
	 */
	private int delay;
	private int retryOnExecuteFailed;
	private int retryBackoffOnExecuteFailed;
	private int retriedTimesOnExecuteFailed;
	private int retryOnNoQualified;
	private int retryBackoffOnNoQualified;
	private int retriedTimesOnNoQualified;

	/**
	 * kryo序列化
	 */
	DelayJob() {
	}
	
	public DelayJob(long id, String uuid, String name, JobType type, String executorName, String jobHandlerName,
			int priority, int weight, LocalDateTime queuedAt, String queuedAtInstance, LocalDateTime lastTrigAt,
			String lastTrigResult, String lastExecuteExecutor, String lastExecuteReturns, boolean lastExecuteSuccess,
			int executeTimeout, LocalDateTime createdAt, String params, boolean parallel, int shard, int delay,
			int retryOnExecuteFailed, int retryBackoffOnExecuteFailed, int retriedTimesOnExecuteFailed,
			int retryOnNoQualified, int retryBackoffOnNoQualified, int retriedTimesOnNoQualified) {
		super(id, uuid, name, type, executorName, jobHandlerName, priority, weight, queuedAt, queuedAtInstance,
				lastTrigAt, lastTrigResult, lastExecuteExecutor, lastExecuteReturns, lastExecuteSuccess, executeTimeout,
				createdAt, params, parallel, 0, 1);
		this.delay = delay;
		this.retryOnExecuteFailed = retryOnExecuteFailed;
		this.retryBackoffOnExecuteFailed = retryBackoffOnExecuteFailed;
		this.retriedTimesOnExecuteFailed = retriedTimesOnExecuteFailed;
		this.retryOnNoQualified = retryOnNoQualified;
		this.retryBackoffOnNoQualified = retryBackoffOnNoQualified;
		this.retriedTimesOnNoQualified = retriedTimesOnNoQualified;
	}

	public static DelayJob of(ExecutableJobBO executableJobBO) {
		DelayBO delay = executableJobBO.getDelay();

		return new DelayJob(executableJobBO.getId(), executableJobBO.getUuid(), executableJobBO.getName(),
				executableJobBO.getType(), executableJobBO.getExecutorName(), executableJobBO.getJobHandlerName(),
				executableJobBO.getPriority(), executableJobBO.getWeight(), executableJobBO.getQueuedAt(),
				executableJobBO.getQueuedAtInstance(), executableJobBO.getLastTrigAt(),
				executableJobBO.getLastTrigResult(), executableJobBO.getLastExecuteExecutor(),
				executableJobBO.getLastExecuteReturns(), executableJobBO.getLastExecuteSuccess(),
				executableJobBO.getExecuteTimeout(), executableJobBO.getCreatedAt(), executableJobBO.getParams(), false,
				0, delay.getDelay(), delay.getRetryOnExecuteFailed(), delay.getRetryBackoffOnExecuteFailed(),
				delay.getRetriedTimesOnExecuteFailed(), delay.getRetryOnNoQualified(),
				delay.getRetryBackoffOnNoQualified(), delay.getRetriedTimesOnNoQualified());
	}

	public int getDelay() {
		return delay;
	}

	public int getRetryOnExecuteFailed() {
		return retryOnExecuteFailed;
	}

	public int getRetryBackoffOnExecuteFailed() {
		return retryBackoffOnExecuteFailed;
	}

	public int getRetriedTimesOnExecuteFailed() {
		return retriedTimesOnExecuteFailed;
	}

	public int getRetryOnNoQualified() {
		return retryOnNoQualified;
	}

	public int getRetryBackoffOnNoQualified() {
		return retryBackoffOnNoQualified;
	}

	public int getRetriedTimesOnNoQualified() {
		return retriedTimesOnNoQualified;
	}

	@Override
	public double ofOverload() {
		DelayBO bo = DelayBO.of(this);
		return getWeight() * bo.rateOfSecond();
	}

	@Override
	public String toString() {
		return "DelayJob [delay=" + delay + ", retryOnExecuteFailed=" + retryOnExecuteFailed
				+ ", retryBackoffOnExecuteFailed=" + retryBackoffOnExecuteFailed + ", retriedTimesOnExecuteFailed="
				+ retriedTimesOnExecuteFailed + ", retryOnNoQualified=" + retryOnNoQualified
				+ ", retryBackoffOnNoQualified=" + retryBackoffOnNoQualified + ", retriedTimesOnNoQualified="
				+ retriedTimesOnNoQualified + ", toString()=" + super.toString() + "]";
	}
	
}
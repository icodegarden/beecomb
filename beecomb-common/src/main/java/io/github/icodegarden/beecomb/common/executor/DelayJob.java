package io.github.icodegarden.beecomb.common.executor;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Setter
@Getter
@ToString(callSuper = true)
public class DelayJob extends Job {
	private static final long serialVersionUID = -8461519222526601867L;

	/**
	 * millis
	 */
	private long delay;
	private int retryOnExecuteFailed;
	private int retryBackoffOnExecuteFailed;
	private int retriedTimesOnExecuteFailed;
	private int retryOnNoQualified;
	private int retryBackoffOnNoQualified;
	private int retriedTimesOnNoQualified;

	/**
	 * kryo序列化
	 */
//	DelayJob() {
//	}
//
//	public DelayJob(long id, String uuid, String name, JobType type, String executorName, String jobHandlerName,
//			int priority, int weight, LocalDateTime queuedAt, String queuedAtInstance, LocalDateTime lastTrigAt,
//			String lastExecuteExecutor, String lastExecuteReturns, boolean lastExecuteSuccess, int executeTimeout,
//			LocalDateTime createdAt, String params, boolean parallel, int shard, long delay, int retryOnExecuteFailed,
//			int retryBackoffOnExecuteFailed, int retriedTimesOnExecuteFailed, int retryOnNoQualified,
//			int retryBackoffOnNoQualified, int retriedTimesOnNoQualified) {
//		super(id, uuid, name, type, executorName, jobHandlerName, priority, weight, queuedAt, queuedAtInstance,
//				lastTrigAt, lastExecuteExecutor, lastExecuteReturns, lastExecuteSuccess, executeTimeout, createdAt,
//				params, parallel, 0, 1);
//		this.delay = delay;
//		this.retryOnExecuteFailed = retryOnExecuteFailed;
//		this.retryBackoffOnExecuteFailed = retryBackoffOnExecuteFailed;
//		this.retriedTimesOnExecuteFailed = retriedTimesOnExecuteFailed;
//		this.retryOnNoQualified = retryOnNoQualified;
//		this.retryBackoffOnNoQualified = retryBackoffOnNoQualified;
//		this.retriedTimesOnNoQualified = retriedTimesOnNoQualified;
//	}

//	public static DelayJob of(ExecutableJobBO executableJobBO) {
//		DelayBO delay = executableJobBO.getDelay();
//
//		return new DelayJob(executableJobBO.getId(), executableJobBO.getUuid(), executableJobBO.getName(),
//				executableJobBO.getType(), executableJobBO.getExecutorName(), executableJobBO.getJobHandlerName(),
//				executableJobBO.getPriority(), executableJobBO.getWeight(), executableJobBO.getQueuedAt(),
//				executableJobBO.getQueuedAtInstance(), executableJobBO.getLastTrigAt(),
//				executableJobBO.getLastExecuteExecutor(), executableJobBO.getLastExecuteReturns(),
//				executableJobBO.getLastExecuteSuccess(), executableJobBO.getExecuteTimeout(),
//				executableJobBO.getCreatedAt(), executableJobBO.getParams(), false, 0, delay.getDelay(),
//				delay.getRetryOnExecuteFailed(), delay.getRetryBackoffOnExecuteFailed(),
//				delay.getRetriedTimesOnExecuteFailed(), delay.getRetryOnNoQualified(),
//				delay.getRetryBackoffOnNoQualified(), delay.getRetriedTimesOnNoQualified());
//	}
	
	public static DelayJob of(ExecutableJobBO executableJobBO) {
		DelayBO delay = executableJobBO.getDelay();

		DelayJob delayJob = new DelayJob();
		BeanUtils.copyProperties(executableJobBO, delayJob);
		BeanUtils.copyProperties(delay, delayJob);
		return delayJob;
	}

	@Override
	public double ofOverload() {
//		DelayBO bo = DelayBO.of(this);
//		return getWeight() * bo.rateOfSecond();
		return getWeight();//对于Executor是实时在运行的
	}

}
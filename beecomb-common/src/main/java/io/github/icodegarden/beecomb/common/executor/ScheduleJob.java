package io.github.icodegarden.beecomb.common.executor;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ScheduleBO;
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
public class ScheduleJob extends Job {
	private static final long serialVersionUID = -8461519222526601868L;

	private Long scheduleFixRate;
	private Long scheduleFixDelay;
	private String sheduleCron;
	private long scheduledTimes;

	/**
	 * kryo序列化
	 */
//	ScheduleJob() {
//	}
//
//	public ScheduleJob(long id, String uuid, String name, JobType type, String executorName, String jobHandlerName,
//			int priority, int weight, LocalDateTime queuedAt, String queuedAtInstance, LocalDateTime lastTrigAt,
//			String lastExecuteExecutor, String lastExecuteReturns, boolean lastExecuteSuccess, int executeTimeout,
//			LocalDateTime createdAt, String params, boolean parallel, int shard, Long scheduleFixRate,
//			Long scheduleFixDelay, String sheduleCron, long scheduledTimes) {
//		super(id, uuid, name, type, executorName, jobHandlerName, priority, weight, queuedAt, queuedAtInstance,
//				lastTrigAt, lastExecuteExecutor, lastExecuteReturns, lastExecuteSuccess, executeTimeout, createdAt,
//				params, parallel, 0, 1);
//		this.scheduleFixRate = scheduleFixRate;
//		this.scheduleFixDelay = scheduleFixDelay;
//		this.sheduleCron = sheduleCron;
//		this.scheduledTimes = scheduledTimes;
//	}

//	public static ScheduleJob of(ExecutableJobBO executableJobBO) {
//		ScheduleBO schedule = executableJobBO.getSchedule();
//		return new ScheduleJob(executableJobBO.getId(), executableJobBO.getUuid(), executableJobBO.getName(),
//				executableJobBO.getType(), executableJobBO.getExecutorName(), executableJobBO.getJobHandlerName(),
//				executableJobBO.getPriority(), executableJobBO.getWeight(), executableJobBO.getQueuedAt(),
//				executableJobBO.getQueuedAtInstance(), executableJobBO.getLastTrigAt(),
//				executableJobBO.getLastExecuteExecutor(), executableJobBO.getLastExecuteReturns(),
//				executableJobBO.getLastExecuteSuccess(), executableJobBO.getExecuteTimeout(),
//				executableJobBO.getCreatedAt(), executableJobBO.getParams(), executableJobBO.getParallel(), 0,
//				schedule.getScheduleFixRate(), schedule.getScheduleFixDelay(), schedule.getSheduleCron(), 0);
//	}
	
	public static ScheduleJob of(ExecutableJobBO executableJobBO) {
		ScheduleBO schedule = executableJobBO.getSchedule();

		ScheduleJob scheduleJob = new ScheduleJob();
		BeanUtils.copyProperties(executableJobBO, scheduleJob);
		BeanUtils.copyProperties(schedule, scheduleJob);
		return scheduleJob;
	}

	@Override
	public double ofOverload() {
//		ScheduleBO bo = ScheduleBO.of(this);
//		return getWeight() * bo.rateOfSecond();
		return getWeight();//对于Executor是实时在运行的
	}

}
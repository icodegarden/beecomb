package io.github.icodegarden.beecomb.common.pojo.biz;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.nutrient.lang.metricsregistry.OverloadCalc;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;

/**
 * 对Executable来说，这里的所有字段只要数据库有值，就要构造进来的
 * 
 * @author Fangfang.Xu
 *
 */
public class ExecutableJobBO implements OverloadCalc, Serializable {
	private static final long serialVersionUID = -3347178855369519453L;

	private Long id;
	private String uuid;
	private String name;
	private JobType type;
	private String executorName;
	private String jobHandlerName;
	private Integer priority;
	private Integer weight;
	private Boolean parallel;
	private Integer maxParallelShards;
	private Boolean queued;
	private LocalDateTime queuedAt;
	private String queuedAtInstance;
	private LocalDateTime lastTrigAt;
	private String lastExecuteExecutor;
	private String lastExecuteReturns;
	private Boolean lastExecuteSuccess;
	private Integer executeTimeout;
	private LocalDateTime nextTrigAt;
	private Boolean end;
	private String createdBy;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private String params;

	private DelayBO delay;
	private ScheduleBO schedule;

	public ExecutableJobBO() {
	}

//	public ExecutableJobBO(Long id, String uuid, String name, JobType type, String executorName, String jobHandlerName,
//			Integer priority, Integer weight, Boolean parallel, Integer maxParallelShards, Boolean queued,
//			LocalDateTime queuedAt, String queuedAtInstance, LocalDateTime lastTrigAt, String lastExecuteExecutor,
//			String lastExecuteReturns, Boolean lastExecuteSuccess, Integer executeTimeout, LocalDateTime nextTrigAt,
//			Boolean end, String createdBy, LocalDateTime createdAt, LocalDateTime updatedAt, String params,
//			DelayBO delay, ScheduleBO schedule) {
//		this.id = id;
//		this.uuid = uuid;
//		this.name = name;
//		this.type = type;
//		this.executorName = executorName;
//		this.jobHandlerName = jobHandlerName;
//		this.priority = priority;
//		this.weight = weight;
//		this.parallel = parallel;
//		this.maxParallelShards = maxParallelShards;
//		this.queued = queued;
//		this.queuedAt = queuedAt;
//		this.queuedAtInstance = queuedAtInstance;
//		this.lastTrigAt = lastTrigAt;
//		this.lastExecuteExecutor = lastExecuteExecutor;
//		this.lastExecuteReturns = lastExecuteReturns;
//		this.lastExecuteSuccess = lastExecuteSuccess;
//		this.executeTimeout = executeTimeout;
//		this.nextTrigAt = nextTrigAt;
//		this.end = end;
//		this.createdBy = createdBy;
//		this.createdAt = createdAt;
//		this.updatedAt = updatedAt;
//		this.params = params;
//		this.delay = delay;
//		this.schedule = schedule;
//	}

	/**
	 * 计算一个任务会形成的负载数<br>
	 * 
	 * @param job
	 * @return
	 */
	@Override
	public double ofOverload() {
		double value = getWeight() * rateOfSecond();
		value = Math.max(value, getWeight() * 0.1);// 最小不能低于10倍容量，以免有大量的超低频率任务时内存溢出
		if (Boolean.TRUE.equals(parallel)) {
			/**
			 * 由于该类是给Worker使用的，并行任务执行时需要对每个分片都使用独立线程，因此*maxParallelShards，使得Worker的用户配置的任务负载数相匹配
			 */
			value *= maxParallelShards;
		}
		return value;
	}

	/**
	 * 每秒执行次数
	 * 
	 * @return
	 */
	private double rateOfSecond() {
		if (getType() == JobType.Delay) {
			return getDelay().rateOfSecond();
		} else if (getType() == JobType.Schedule) {
			ScheduleBO schedule = getSchedule();
			return schedule.rateOfSecond();
		}
		throw new IllegalArgumentException(String.format("jobtype [%s] not support", getType()));
	}

	/**
	 * 计算出下一次任务触发时间<br>
	 * <h1>只适用于OnEnQueue</h1> <br>
	 * 
	 * delay类型：结果时间可能早于当前<br>
	 * 任务创建时=创建或修改时间（他们2个相等）+延迟<br>
	 * 修改其他字段（不会引起重进队列）=若此后worker重启，重进队列时间要按创建时间+延迟（不能是修改时间+延迟）或 nextTrigAt<br>
	 * 修改时间字段（需要重进队列）=修改时间+延迟（不能是创建时间+延迟，上次得出的nextTrigAt必须清空）<br>
	 * 重进队列=修改时间+延迟 或 上次得出的nextTrigAt<br>
	 *
	 * 综上总结：<br>
	 * 任务创建时=修改时间+延迟<br>
	 * 修改其他字段（不会引起重进队列）=nextTrigAt<br>
	 * 修改时间字段（需要重进队列）=修改时间+延迟<br>
	 * 重进队列=nextTrigAt<br>
	 * 见本方法代码实现<br>
	 *
	 * <br>
	 * schedule类型：{@link ScheduleBO#calcNextTrigAt()}<br>
	 * 
	 * @return
	 */
	@JsonIgnore
	public LocalDateTime calcNextTrigAtOnEnQueue() {
		if (delay != null) {
			if (nextTrigAt == null) {
				/*
				 * 创建任务 或 修改时间字段
				 */
				return updatedAt.plus(delay.getDelay(), ChronoUnit.MILLIS);
			}
			/*
			 * 重进队列<br>
			 * 特别说明：worker重启时，
			 */
			return nextTrigAt;
		}
		if (schedule != null) {
			return schedule.calcNextTrigAtOnEnQueue();
		}
		throw new IllegalArgumentException("delay or schedule must not null");
	}

	/**
	 * 计算出距离下一次任务触发的毫秒<br>
	 * <h1>只适用于OnEnQueue</h1> <br>
	 * 
	 * @return 时间可能早于当前
	 */
	@JsonIgnore
	public long calcNextTrigDelayMillisOnEnQueue() {
		LocalDateTime next = calcNextTrigAtOnEnQueue();
		Duration between = Duration.between(SystemUtils.now(), next);
		return between.toMillis();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JobType getType() {
		return type;
	}

	public void setType(JobType type) {
		this.type = type;
	}

	public String getExecutorName() {
		return executorName;
	}

	public void setExecutorName(String executorName) {
		this.executorName = executorName;
	}

	public String getJobHandlerName() {
		return jobHandlerName;
	}

	public void setJobHandlerName(String jobHandlerName) {
		this.jobHandlerName = jobHandlerName;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Boolean getParallel() {
		return parallel;
	}

	public void setParallel(Boolean parallel) {
		this.parallel = parallel;
	}

	public Integer getMaxParallelShards() {
		return maxParallelShards;
	}

	public void setMaxParallelShards(Integer maxParallelShards) {
		this.maxParallelShards = maxParallelShards;
	}

	public Boolean getQueued() {
		return queued;
	}

	public void setQueued(Boolean queued) {
		this.queued = queued;
	}

	public LocalDateTime getQueuedAt() {
		return queuedAt;
	}

	public void setQueuedAt(LocalDateTime queuedAt) {
		this.queuedAt = queuedAt;
	}

	public String getQueuedAtInstance() {
		return queuedAtInstance;
	}

	public void setQueuedAtInstance(String queuedAtInstance) {
		this.queuedAtInstance = queuedAtInstance;
	}

	public LocalDateTime getLastTrigAt() {
		return lastTrigAt;
	}

	public void setLastTrigAt(LocalDateTime lastTrigAt) {
		this.lastTrigAt = lastTrigAt;
	}

	public String getLastExecuteExecutor() {
		return lastExecuteExecutor;
	}

	public void setLastExecuteExecutor(String lastExecuteExecutor) {
		this.lastExecuteExecutor = lastExecuteExecutor;
	}

	public String getLastExecuteReturns() {
		return lastExecuteReturns;
	}

	public void setLastExecuteReturns(String lastExecuteReturns) {
		this.lastExecuteReturns = lastExecuteReturns;
	}

	public Boolean getLastExecuteSuccess() {
		return lastExecuteSuccess;
	}

	public void setLastExecuteSuccess(Boolean lastExecuteSuccess) {
		this.lastExecuteSuccess = lastExecuteSuccess;
	}

	public Integer getExecuteTimeout() {
		return executeTimeout;
	}

	public void setExecuteTimeout(Integer executeTimeout) {
		this.executeTimeout = executeTimeout;
	}

	public LocalDateTime getNextTrigAt() {
		return nextTrigAt;
	}

	public void setNextTrigAt(LocalDateTime nextTrigAt) {
		this.nextTrigAt = nextTrigAt;
	}

	public Boolean getEnd() {
		return end;
	}

	public void setEnd(Boolean end) {
		this.end = end;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public DelayBO getDelay() {
		return delay;
	}

	public void setDelay(DelayBO delay) {
		this.delay = delay;
	}

	public ScheduleBO getSchedule() {
		return schedule;
	}

	public void setSchedule(ScheduleBO schedule) {
		this.schedule = schedule;
	}

	@Override
	public String toString() {
		return "[id=" + id + ", uuid=" + uuid + ", name=" + name + ", type=" + type + ", executorName=" + executorName
				+ ", jobHandlerName=" + jobHandlerName + ", parallel=" + parallel + "]";
	}

	public String toStringFull() {
		return "ExecutableJobBO [id=" + id + ", uuid=" + uuid + ", name=" + name + ", type=" + type + ", executorName="
				+ executorName + ", jobHandlerName=" + jobHandlerName + ", priority=" + priority + ", weight=" + weight
				+ ", parallel=" + parallel + ", maxParallelShards=" + maxParallelShards + ", queued=" + queued
				+ ", queuedAt=" + queuedAt + ", queuedAtInstance=" + queuedAtInstance + ", lastTrigAt=" + lastTrigAt
				+ ", lastExecuteExecutor=" + lastExecuteExecutor + ", lastExecuteReturns=" + lastExecuteReturns
				+ ", lastExecuteSuccess=" + lastExecuteSuccess + ", executeTimeout=" + executeTimeout + ", nextTrigAt="
				+ nextTrigAt + ", end=" + end + ", createdBy=" + createdBy + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + ", params=" + params + ", delay=" + delay + ", schedule=" + schedule + "]";
	}

}

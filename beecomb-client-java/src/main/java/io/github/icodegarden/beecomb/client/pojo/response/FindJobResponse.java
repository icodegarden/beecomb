package io.github.icodegarden.beecomb.client.pojo.response;

import java.time.LocalDateTime;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.commons.lang.annotation.NotNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class FindJobResponse {

	/**
	 * main
	 */
	@NotNull
	private Long id;
	private String uuid;
	@NotNull
	private String name;
	@NotNull
	private JobType type;
	@NotNull
	private String executorName;
	@NotNull
	private String jobHandlerName;
	@NotNull
	private Integer priority;
	@NotNull
	private Integer weight;
	@NotNull
	private Boolean parallel;
	@NotNull
	private Integer maxParallelShards;
	@NotNull
	private Boolean queued;
	private LocalDateTime queuedAt;
	private String queuedAtInstance;
	private LocalDateTime lastTrigAt;
	private String lastTrigResult;
	private String lastExecuteExecutor;
	private String lastExecuteReturns;
	private Boolean lastExecuteSuccess;
	@NotNull
	private Integer executeTimeout;
	private LocalDateTime nextTrigAt;
	@NotNull
	private Boolean end;
	@NotNull
	private String createdBy;
	@NotNull
	private LocalDateTime createdAt;

	/**
	 * detail
	 */
	private String params;
	private String desc;

	/**
	 * delay
	 */
	private Integer delay;
	private Integer retryOnExecuteFailed;
	private Integer retryBackoffOnExecuteFailed;
	private Integer retriedTimesOnExecuteFailed;
	private Integer retryOnNoQualified;
	private Integer retryBackoffOnNoQualified;
	private Integer retriedTimesOnNoQualified;

	/**
	 * schedule
	 */
	private Integer scheduleFixRate;
	private Integer scheduleFixDelay;
	private String sheduleCron;
	private Long scheduledTimes;

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

	public String getLastTrigResult() {
		return lastTrigResult;
	}

	public void setLastTrigResult(String lastTrigResult) {
		this.lastTrigResult = lastTrigResult;
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

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Integer getDelay() {
		return delay;
	}

	public void setDelay(Integer delay) {
		this.delay = delay;
	}

	public Integer getRetryOnExecuteFailed() {
		return retryOnExecuteFailed;
	}

	public void setRetryOnExecuteFailed(Integer retryOnExecuteFailed) {
		this.retryOnExecuteFailed = retryOnExecuteFailed;
	}

	public Integer getRetryBackoffOnExecuteFailed() {
		return retryBackoffOnExecuteFailed;
	}

	public void setRetryBackoffOnExecuteFailed(Integer retryBackoffOnExecuteFailed) {
		this.retryBackoffOnExecuteFailed = retryBackoffOnExecuteFailed;
	}

	public Integer getRetriedTimesOnExecuteFailed() {
		return retriedTimesOnExecuteFailed;
	}

	public void setRetriedTimesOnExecuteFailed(Integer retriedTimesOnExecuteFailed) {
		this.retriedTimesOnExecuteFailed = retriedTimesOnExecuteFailed;
	}

	public Integer getRetryOnNoQualified() {
		return retryOnNoQualified;
	}

	public void setRetryOnNoQualified(Integer retryOnNoQualified) {
		this.retryOnNoQualified = retryOnNoQualified;
	}

	public Integer getRetryBackoffOnNoQualified() {
		return retryBackoffOnNoQualified;
	}

	public void setRetryBackoffOnNoQualified(Integer retryBackoffOnNoQualified) {
		this.retryBackoffOnNoQualified = retryBackoffOnNoQualified;
	}

	public Integer getRetriedTimesOnNoQualified() {
		return retriedTimesOnNoQualified;
	}

	public void setRetriedTimesOnNoQualified(Integer retriedTimesOnNoQualified) {
		this.retriedTimesOnNoQualified = retriedTimesOnNoQualified;
	}

	public Integer getScheduleFixRate() {
		return scheduleFixRate;
	}

	public void setScheduleFixRate(Integer scheduleFixRate) {
		this.scheduleFixRate = scheduleFixRate;
	}

	public Integer getScheduleFixDelay() {
		return scheduleFixDelay;
	}

	public void setScheduleFixDelay(Integer scheduleFixDelay) {
		this.scheduleFixDelay = scheduleFixDelay;
	}

	public String getSheduleCron() {
		return sheduleCron;
	}

	public void setSheduleCron(String sheduleCron) {
		this.sheduleCron = sheduleCron;
	}

	public Long getScheduledTimes() {
		return scheduledTimes;
	}

	public void setScheduledTimes(Long scheduledTimes) {
		this.scheduledTimes = scheduledTimes;
	}

	@Override
	public String toString() {
		return "FindJobResponse [id=" + id + ", uuid=" + uuid + ", name=" + name + ", type=" + type + ", executorName="
				+ executorName + ", jobHandlerName=" + jobHandlerName + ", priority=" + priority + ", weight=" + weight
				+ ", parallel=" + parallel + ", maxParallelShards=" + maxParallelShards + ", queued=" + queued
				+ ", queuedAt=" + queuedAt + ", queuedAtInstance=" + queuedAtInstance + ", lastTrigAt=" + lastTrigAt
				+ ", lastTrigResult=" + lastTrigResult + ", lastExecuteExecutor=" + lastExecuteExecutor
				+ ", lastExecuteReturns=" + lastExecuteReturns + ", lastExecuteSuccess=" + lastExecuteSuccess
				+ ", executeTimeout=" + executeTimeout + ", nextTrigAt=" + nextTrigAt + ", end=" + end + ", createdBy="
				+ createdBy + ", createdAt=" + createdAt + ", params=" + params + ", desc=" + desc + ", delay=" + delay
				+ ", retryOnExecuteFailed=" + retryOnExecuteFailed + ", retryBackoffOnExecuteFailed="
				+ retryBackoffOnExecuteFailed + ", retriedTimesOnExecuteFailed=" + retriedTimesOnExecuteFailed
				+ ", retryOnNoQualified=" + retryOnNoQualified + ", retryBackoffOnNoQualified="
				+ retryBackoffOnNoQualified + ", retriedTimesOnNoQualified=" + retriedTimesOnNoQualified
				+ ", scheduleFixRate=" + scheduleFixRate + ", scheduleFixDelay=" + scheduleFixDelay + ", sheduleCron="
				+ sheduleCron + ", scheduledTimes=" + scheduledTimes + "]";
	}

}

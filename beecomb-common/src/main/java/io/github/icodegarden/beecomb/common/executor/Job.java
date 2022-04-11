package io.github.icodegarden.beecomb.common.executor;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.commons.lang.annotation.NotNull;
import io.github.icodegarden.commons.lang.metrics.OverloadCalc;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class Job implements OverloadCalc, Serializable {
	private static final long serialVersionUID = -3347178855369519454L;

	private long id;
	private String uuid;
	@NotNull
	private String name;
	@NotNull
	private JobType type;
	@NotNull
	private String executorName;
	@NotNull
	private String jobHandlerName;
	private int priority;
	private int weight;
	@NotNull
	private LocalDateTime queuedAt;
	@NotNull
	private String queuedAtInstance;
	private LocalDateTime lastTrigAt;
	private String lastTrigResult;
	private String lastExecuteExecutor;
	private String lastExecuteReturns;
	private boolean lastExecuteSuccess;
	private int executeTimeout;
	@NotNull
	private LocalDateTime createdAt;

	private String params;

	private boolean parallel;
	/**
	 * from 0<br>
	 * current executor's shard number<br>
	 * only use on parallel is true<br>
	 * 
	 */
	private int shard;
	private int shardTotal;

	/**
	 * kryo序列化
	 */
	Job() {
	}

	public Job(long id, String uuid, String name, JobType type, String executorName, String jobHandlerName,
			int priority, int weight, LocalDateTime queuedAt, String queuedAtInstance, LocalDateTime lastTrigAt,
			String lastTrigResult, String lastExecuteExecutor, String lastExecuteReturns, boolean lastExecuteSuccess,
			int executeTimeout, LocalDateTime createdAt, String params, boolean parallel, int shard, int shardTotal) {
		this.id = id;
		this.uuid = uuid;
		this.name = name;
		this.type = type;
		this.executorName = executorName;
		this.jobHandlerName = jobHandlerName;
		this.priority = priority;
		this.weight = weight;
		this.queuedAt = queuedAt;
		this.queuedAtInstance = queuedAtInstance;
		this.lastTrigAt = lastTrigAt;
		this.lastTrigResult = lastTrigResult;
		this.lastExecuteExecutor = lastExecuteExecutor;
		this.lastExecuteReturns = lastExecuteReturns;
		this.lastExecuteSuccess = lastExecuteSuccess;
		this.executeTimeout = executeTimeout;
		this.createdAt = createdAt;
		this.params = params;
		this.parallel = parallel;
		this.shard = shard;
		this.shardTotal = shardTotal;
	}

	public long getId() {
		return id;
	}

	public String getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public JobType getType() {
		return type;
	}

	public String getExecutorName() {
		return executorName;
	}

	public String getJobHandlerName() {
		return jobHandlerName;
	}

	public int getPriority() {
		return priority;
	}

	public int getWeight() {
		return weight;
	}

	public LocalDateTime getQueuedAt() {
		return queuedAt;
	}

	public String getQueuedAtInstance() {
		return queuedAtInstance;
	}

	public LocalDateTime getLastTrigAt() {
		return lastTrigAt;
	}

	public String getLastTrigResult() {
		return lastTrigResult;
	}

	public String getLastExecuteExecutor() {
		return lastExecuteExecutor;
	}

	public String getLastExecuteReturns() {
		return lastExecuteReturns;
	}

	public boolean isLastExecuteSuccess() {
		return lastExecuteSuccess;
	}

	public int getExecuteTimeout() {
		return executeTimeout;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public String getParams() {
		return params;
	}

	public boolean isParallel() {
		return parallel;
	}

	public int getShard() {
		return shard;
	}

	public void setShard(int shard) {
		this.shard = shard;
	}

	public int getShardTotal() {
		return shardTotal;
	}
	
	public void setShardTotal(int shardTotal) {
		this.shardTotal = shardTotal;
	}
	
	@Override
	public String toString() {
		return "Job [id=" + id + ", uuid=" + uuid + ", name=" + name + ", type=" + type + ", executorName="
				+ executorName + ", jobHandlerName=" + jobHandlerName + ", priority=" + priority + ", weight=" + weight
				+ ", queuedAt=" + queuedAt + ", queuedAtInstance=" + queuedAtInstance + ", lastTrigAt=" + lastTrigAt
				+ ", lastTrigResult=" + lastTrigResult + ", lastExecuteExecutor=" + lastExecuteExecutor
				+ ", lastExecuteReturns=" + lastExecuteReturns + ", lastExecuteSuccess=" + lastExecuteSuccess
				+ ", executeTimeout=" + executeTimeout + ", createdAt=" + createdAt + ", params=" + params
				+ ", parallel=" + parallel + ", shard=" + shard + ", shardTotal=" + shardTotal + "]";
	}
	
}
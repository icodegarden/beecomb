package io.github.icodegarden.beecomb.common.executor;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.nutrient.lang.ShardObject;
import io.github.icodegarden.nutrient.lang.annotation.NotNull;
import io.github.icodegarden.nutrient.lang.metricsregistry.OverloadCalc;
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
@ToString
public abstract class Job implements ShardObject, OverloadCalc, Serializable {
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

//	/**
//	 * kryo序列化
//	 */
//	Job() {
//	}
//
//	public Job(long id, String uuid, String name, JobType type, String executorName, String jobHandlerName,
//			int priority, int weight, LocalDateTime queuedAt, String queuedAtInstance, LocalDateTime lastTrigAt,
//			String lastExecuteExecutor, String lastExecuteReturns, boolean lastExecuteSuccess, int executeTimeout,
//			LocalDateTime createdAt, String params, boolean parallel, int shard, int shardTotal) {
//		this.id = id;
//		this.uuid = uuid;
//		this.name = name;
//		this.type = type;
//		this.executorName = executorName;
//		this.jobHandlerName = jobHandlerName;
//		this.priority = priority;
//		this.weight = weight;
//		this.queuedAt = queuedAt;
//		this.queuedAtInstance = queuedAtInstance;
//		this.lastTrigAt = lastTrigAt;
//		this.lastExecuteExecutor = lastExecuteExecutor;
//		this.lastExecuteReturns = lastExecuteReturns;
//		this.lastExecuteSuccess = lastExecuteSuccess;
//		this.executeTimeout = executeTimeout;
//		this.createdAt = createdAt;
//		this.params = params;
//		this.parallel = parallel;
//		this.shard = shard;
//		this.shardTotal = shardTotal;
//	}

}
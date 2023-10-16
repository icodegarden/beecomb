package io.github.icodegarden.beecomb.client.pojo.transfer;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.nutrient.lang.annotation.NotEmpty;
import io.github.icodegarden.nutrient.lang.annotation.NotNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CreateJobDTO {

	/**
	 * 任务的uuid，注意beecomb并不会保证该值唯一性，而是由用户自己决定，uuid可以重复
	 */
	private String uuid;
	/**
	 * 任务名称
	 */
	@NotEmpty
	private String name;
	@NotNull
	private JobType type;
	/**
	 * 执行器名称，该任务由哪个Executor处理
	 */
	@NotNull
	private String executorName;
	/**
	 * jobHandler名称，该任务由哪个执行器的jobHandler处理
	 */
	@NotNull
	private String jobHandlerName;
	/**
	 * 任务的优先级，默认5，仅在任务恢复时起作用
	 */
	private Integer priority;
	/**
	 * 任务的重量，默认1，该值对负载压力的计算起作用，例如Executor配置的overload.jobs.max是10000，则Executor能负载10000个重量是1、执行频率是1秒1次的任务，或负载4000个重量是5、执行频频率2秒1次的任务
	 */
	private Integer weight;
	/**
	 * 是否并行任务，默认false
	 */
	private Boolean parallel;
	/**
	 * 最大并行分片数，默认8，当合格的Executor数大于等于该值时，按该值分片，小于时按实际Executor数分片
	 */
	private Integer maxParallelShards;
	/**
	 * 任务执行超时毫秒，默认10000
	 */
	private Integer executeTimeout;
	/**
	 * 任务执行的参数，最大65535
	 */
	private String params;
	/**
	 * 任务描述，最大200
	 */
	private String desc;

	public CreateJobDTO(String name, JobType type, String executorName, String jobHandlerName) {
		this.name = name;
		this.type = type;
		this.executorName = executorName;
		this.jobHandlerName = jobHandlerName;
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

	public Integer getExecuteTimeout() {
		return executeTimeout;
	}

	public void setExecuteTimeout(Integer executeTimeout) {
		this.executeTimeout = executeTimeout;
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

	@Override
	public String toString() {
		return "BaseJob [uuid=" + uuid + ", name=" + name + ", type=" + type + ", executorName=" + executorName
				+ ", jobHandlerName=" + jobHandlerName + ", priority=" + priority + ", weight=" + weight + ", parallel="
				+ parallel + ", maxParallelShards=" + maxParallelShards + ", executeTimeout=" + executeTimeout
				+ ", params=" + params + ", desc=" + desc + "]";
	}

}

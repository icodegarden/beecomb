package io.github.icodegarden.beecomb.client.pojo.transfer;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.commons.lang.annotation.NotEmpty;
import io.github.icodegarden.commons.lang.annotation.NotNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CreateJobDTO {

	private String uuid;
	@NotEmpty
	private String name;
	@NotNull
	private JobType type;
	@NotNull
	private String executorName;
	@NotNull
	private String jobHandlerName;
	private Integer priority;
	private Integer weight;
	private Boolean parallel;
	private Integer maxParallelShards;
	private Integer executeTimeout;
	private String params;
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

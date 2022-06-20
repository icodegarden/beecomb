package io.github.icodegarden.beecomb.client.pojo.transfer;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.annotation.Length;
import io.github.icodegarden.commons.lang.annotation.NotNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class UpdateJobDTO {

	@NotNull
	private final Long id;

	@Length(max = 30)
	private String name;
	@Length(max = 30)
	private String executorName;
	@Length(max = 30)
	private String jobHandlerName;
	private Integer priority;
	private Integer weight;
	private Integer maxParallelShards;
	private Integer executeTimeout;
	@Length(max = 65535)
	private String params;
	@Length(max = 200)
	private String desc;

	public UpdateJobDTO(Long id) {
		Assert.notNull(id, "id must not null");
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "UpdateJobDTO [id=" + id + ", name=" + name + ", executorName=" + executorName + ", jobHandlerName="
				+ jobHandlerName + ", priority=" + priority + ", weight=" + weight + ", maxParallelShards="
				+ maxParallelShards + ", executeTimeout=" + executeTimeout + ", params=" + params + ", desc=" + desc
				+ "]";
	}

}

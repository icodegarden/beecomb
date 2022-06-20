package io.github.icodegarden.beecomb.client.pojo.view;

import io.github.icodegarden.beecomb.common.enums.JobType;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class CreateJobVO {

	private Job job;
	private String dispatchException;

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public String getDispatchException() {
		return dispatchException;
	}

	public void setDispatchException(String dispatchException) {
		this.dispatchException = dispatchException;
	}

	@Override
	public String toString() {
		return "CreateJobVO [job=" + job + ", dispatchException=" + dispatchException + "]";
	}

	public static class Job {
		private Long id;
		private String uuid;
		private String name;
		private JobType type;
		private Integer priority;
		private Integer weight;
		private Boolean queued;
		/**
		 * 若使用async方式，则该字段是null
		 */
		private String queuedAtInstance;

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

		public Boolean getQueued() {
			return queued;
		}

		public void setQueued(Boolean queued) {
			this.queued = queued;
		}

		public String getQueuedAtInstance() {
			return queuedAtInstance;
		}

		public void setQueuedAtInstance(String queuedAtInstance) {
			this.queuedAtInstance = queuedAtInstance;
		}

		@Override
		public String toString() {
			return "Job [id=" + id + ", uuid=" + uuid + ", name=" + name + ", type=" + type + ", priority=" + priority
					+ ", weight=" + weight + ", queued=" + queued + ", queuedAtInstance=" + queuedAtInstance + "]";
		}

	}
}

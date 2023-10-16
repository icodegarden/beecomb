package io.github.icodegarden.beecomb.client.pojo.transfer;

import org.springframework.util.Assert;

import io.github.icodegarden.nutrient.lang.annotation.Length;
import io.github.icodegarden.nutrient.lang.annotation.NotNull;

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

	private Delay delay;
	private Schedule schedule;

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

	public Delay getDelay() {
		return delay;
	}

	public void setDelay(Delay delay) {
		this.delay = delay;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	@Override
	public String toString() {
		return "UpdateJobDTO [id=" + id + ", name=" + name + ", executorName=" + executorName + ", jobHandlerName="
				+ jobHandlerName + ", priority=" + priority + ", weight=" + weight + ", maxParallelShards="
				+ maxParallelShards + ", executeTimeout=" + executeTimeout + ", params=" + params + ", desc=" + desc
				+ ", delay=" + delay + ", schedule=" + schedule + "]";
	}

	public static class Delay {
		private Long delay;
		private Integer retryOnExecuteFailed;
		private Integer retryBackoffOnExecuteFailed;
		private Integer retryOnNoQualified;
		private Integer retryBackoffOnNoQualified;

		public Long getDelay() {
			return delay;
		}

		public void setDelay(Long delay) {
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

		@Override
		public String toString() {
			return "Delay [delay=" + delay + ", retryOnExecuteFailed=" + retryOnExecuteFailed
					+ ", retryBackoffOnExecuteFailed=" + retryBackoffOnExecuteFailed + ", retryOnNoQualified="
					+ retryOnNoQualified + ", retryBackoffOnNoQualified=" + retryBackoffOnNoQualified + "]";
		}
	}

	public static class Schedule {
		private Long scheduleFixRate;
		private Long scheduleFixDelay;
		private String sheduleCron;

		public Long getScheduleFixRate() {
			return scheduleFixRate;
		}

		public void setScheduleFixRate(Long scheduleFixRate) {
			this.scheduleFixRate = scheduleFixRate;
		}

		public Long getScheduleFixDelay() {
			return scheduleFixDelay;
		}

		public void setScheduleFixDelay(Long scheduleFixDelay) {
			this.scheduleFixDelay = scheduleFixDelay;
		}

		public String getSheduleCron() {
			return sheduleCron;
		}

		public void setSheduleCron(String sheduleCron) {
			this.sheduleCron = sheduleCron;
		}

		@Override
		public String toString() {
			return "Schedule [scheduleFixRate=" + scheduleFixRate + ", scheduleFixDelay=" + scheduleFixDelay
					+ ", sheduleCron=" + sheduleCron + "]";
		}

	}
}

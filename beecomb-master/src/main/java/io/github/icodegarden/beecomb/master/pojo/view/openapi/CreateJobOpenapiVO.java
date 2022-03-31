package io.github.icodegarden.beecomb.master.pojo.view.openapi;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Builder
@Getter
@ToString
public class CreateJobOpenapiVO {

	/**
	 * 失败时null
	 */
	private Job job;
	/**
	 * dispatch失败时有
	 */
	private String dispatchException;

	@Getter
	@ToString
	public static class Job {
		private Long id;
		private String uuid;
		private String name;
		private JobType type;
		private Integer priority;
		private Integer weight;
		private Boolean queued;
		private String queuedAtInstance;

		public static Job of(ExecutableJobBO one) {
			Job job = new Job();
			job.id = one.getId();
			job.uuid = one.getUuid();
			job.name = one.getName();
			job.type = one.getType();
			job.priority = one.getPriority();
			job.weight = one.getWeight();
			job.queued = one.getQueued();
			job.queuedAtInstance = one.getQueuedAtInstance();

			return job;
		}
	}
}

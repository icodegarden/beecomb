package io.github.icodegarden.beecomb.master.pojo.view;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import lombok.Data;

/**
 * 这个类是必须的，不能直接使用JobVO
 * @author Fangfang.Xu
 *
 */
@Data
public class CreateJobVO {

	private Job job;
	private String dispatchException;

	@Data
	public static class Job {
		private Long id;
		private String uuid;
		private String name;
		private JobType type;
		private Integer priority;
		private Integer weight;
		private Boolean queued;
		private String queuedAtInstance;

		public Job(ExecutableJobBO job) {
			this.id = job.getId();
			this.uuid = job.getUuid();
			this.name = job.getName();
			this.type = job.getType();
			this.priority = job.getPriority();
			this.weight = job.getWeight();
			this.queued = job.getQueued();
			this.queuedAtInstance = job.getQueuedAtInstance();
		}
	}
}

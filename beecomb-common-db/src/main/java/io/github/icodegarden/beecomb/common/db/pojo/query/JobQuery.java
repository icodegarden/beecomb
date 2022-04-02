package io.github.icodegarden.beecomb.common.db.pojo.query;

import java.time.LocalDateTime;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.query.BaseQuery;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Fangfang.Xu
 *
 */

@Getter
@Setter
@ToString
public class JobQuery extends BaseQuery {

	private String uuid;
	private Boolean queued;
	private Boolean end;
	private String createdBy;
	private String nameLike;
	private JobType type;
	private Boolean parallel;
	private Boolean lastExecuteSuccess;
	private LocalDateTime createdAtGte;
	private LocalDateTime createdAtLte;
	private LocalDateTime lastTrigAtGte;
	private LocalDateTime lastTrigAtLte;

	/**
	 * 
	 */
	private LocalDateTime nextTrigAtLt;

	private With with;

	@Builder
	public JobQuery(int page, int size, String sort, String limit, String uuid, Boolean queued, Boolean end,
			String createdBy, String nameLike, JobType type, Boolean parallel, Boolean lastExecuteSuccess,
			LocalDateTime createdAtGte, LocalDateTime createdAtLte, LocalDateTime lastTrigAtGte,
			LocalDateTime lastTrigAtLte, LocalDateTime nextTrigAtLt, With with) {
		super(page, size, sort, limit);
		this.uuid = uuid;
		this.queued = queued;
		this.end = end;
		this.createdBy = createdBy;
		this.nameLike = nameLike;
		this.type = type;
		this.parallel = parallel;
		this.lastExecuteSuccess = lastExecuteSuccess;
		this.createdAtGte = createdAtGte;
		this.createdAtLte = createdAtLte;
		this.lastTrigAtGte = lastTrigAtGte;
		this.lastTrigAtLte = lastTrigAtLte;
		this.nextTrigAtLt = nextTrigAtLt;
		this.with = with;
	}

	@Builder
	@Data
	public static class With {

		public static final With WITH_LEAST = With.builder().build();
		public static final With WITH_MOST = With.builder()
				.jobMain(JobMain.builder().queuedAt(true).queuedAtInstance(true).lastTrigResult(true)
						.lastExecuteExecutor(true).lastExecuteReturns(true).createdBy(true).createdAt(true).build())
				.jobDetail(JobDetail.builder().params(true).desc(true).build()).delayJob(DelayJob.builder().build())
				.scheduleJob(ScheduleJob.builder().build()).build();
		public static final With WITH_EXECUTABLE = With.builder()
				.jobMain(JobMain.builder().createdAt(true).lastExecuteExecutor(true).lastExecuteReturns(true)
						.lastTrigResult(true).build())
				.jobDetail(JobDetail.builder().params(true).build()).delayJob(DelayJob.builder().build())
				.scheduleJob(ScheduleJob.builder().build()).build();

		private JobMain jobMain;
		private JobDetail jobDetail;
		private DelayJob delayJob;
		private ScheduleJob scheduleJob;

		@Builder
		@Data
		public static class JobMain {
			private boolean queuedAt;
			private boolean queuedAtInstance;
			private boolean lastTrigResult;
			private boolean lastExecuteExecutor;
			private boolean lastExecuteReturns;
			private boolean createdBy;
			private boolean createdAt;
		}

		@Builder
		@Data
		public static class JobDetail {
			private boolean params;
			private boolean desc;
		}

		@Builder
		@Data
		public static class DelayJob {
		}

		@Builder
		@Data
		public static class ScheduleJob {
		}

	}

}

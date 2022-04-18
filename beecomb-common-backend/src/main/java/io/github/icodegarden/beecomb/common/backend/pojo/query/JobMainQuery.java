package io.github.icodegarden.beecomb.common.backend.pojo.query;

import java.time.LocalDateTime;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.query.BaseQuery;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Fangfang.Xu
 *
 */
@Getter
@ToString
public class JobMainQuery extends BaseQuery {

	private Long id;
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
	public JobMainQuery(int page, int size, String sort, String limit, Long id, String uuid, Boolean queued,
			Boolean end, String createdBy, String nameLike, JobType type, Boolean parallel, Boolean lastExecuteSuccess,
			LocalDateTime createdAtGte, LocalDateTime createdAtLte, LocalDateTime lastTrigAtGte,
			LocalDateTime lastTrigAtLte, LocalDateTime nextTrigAtLt, With with) {
		super(page, size, sort, limit);
		this.id = id;
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
	@Getter
	@ToString
	public static class With {
		public static final With WITH_LEAST = With.builder().build();

		public static final With WITH_MOST = With.builder().queuedAt(true).queuedAtInstance(true).lastTrigResult(true)
				.lastExecuteExecutor(true).lastExecuteReturns(true).createdBy(true).createdAt(true)
				.jobDetail(JobDetailQuery.With.builder().params(true).desc(true).build())
				.delayJob(DelayJobQuery.With.builder().build()).scheduleJob(ScheduleJobQuery.With.builder().build())
				.build();

		public static final With WITH_EXECUTABLE = With.builder().createdAt(true).lastExecuteExecutor(true)
				.lastExecuteReturns(true).lastTrigResult(true)
				.jobDetail(JobDetailQuery.With.builder().params(true).build())
				.delayJob(DelayJobQuery.With.builder().build()).scheduleJob(ScheduleJobQuery.With.builder().build())
				.build();

		private boolean queuedAt;
		private boolean queuedAtInstance;
		private boolean lastTrigResult;
		private boolean lastExecuteExecutor;
		private boolean lastExecuteReturns;
		private boolean createdBy;
		private boolean createdAt;

		private JobDetailQuery.With jobDetail;
		private DelayJobQuery.With delayJob;
		private ScheduleJobQuery.With scheduleJob;

	}

}

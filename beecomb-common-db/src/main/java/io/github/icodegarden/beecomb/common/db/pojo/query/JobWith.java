package io.github.icodegarden.beecomb.common.db.pojo.query;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Fangfang.Xu
 *
 */
@Builder
@Data
public class JobWith {

	public static final JobWith WITH_LEAST = JobWith.builder().build();
	public static final JobWith WITH_MOST = JobWith.builder()
			.jobMain(JobMain.builder().queuedAt(true).queuedAtInstance(true).lastTrigResult(true)
					.lastExecuteExecutor(true).lastExecuteReturns(true).createdBy(true).createdAt(true).build())
			.jobDetail(JobDetail.builder().params(true).desc(true).build()).delayJob(DelayJob.builder().build())
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

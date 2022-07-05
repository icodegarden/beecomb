package io.github.icodegarden.beecomb.common.backend.pojo.persistence;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class ScheduleJobPO {

	private Long jobId;// bigint NOT NULL,
	private Long scheduleFixRate;
	private Long scheduleFixDelay;
	private String sheduleCron;// varchar(20),
	private Long scheduledTimes;// bigint,

	@Data
	public static class Update{
		@NonNull
		private Long jobId;// bigint NOT NULL,
		
		private Long scheduleFixRate;
		private Long scheduleFixDelay;
		private String sheduleCron;// varchar(20),
		
		public Update() {}
		
		@Builder
		public Update(@NonNull Long jobId, Long scheduleFixRate, Long scheduleFixDelay, String sheduleCron) {
			super();
			this.jobId = jobId;
			this.scheduleFixRate = scheduleFixRate;
			this.scheduleFixDelay = scheduleFixDelay;
			this.sheduleCron = sheduleCron;
		}
	}
}

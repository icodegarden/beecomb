package io.github.icodegarden.beecomb.common.db.pojo.persistence;

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
	private Integer scheduleFixRate;// int comment 'ms',
	private Integer scheduleFixDelay;// int comment 'ms',
	private String sheduleCron;// varchar(20),
	private Long scheduledTimes;// bigint,

	@Builder
	@Data
	public static class Update{
		@NonNull
		private Long jobId;// bigint NOT NULL,
		private Integer scheduleFixRate;// int comment 'ms',
		private Integer scheduleFixDelay;// int comment 'ms',
		private String sheduleCron;// varchar(20),
	}
}

package io.github.icodegarden.beecomb.common.db.pojo.transfer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Setter
@Getter
@ToString
public class CreateScheduleJobDTO {
	
	private Long jobId;
	private Integer scheduleFixRate;// int comment 'ms',
	private Integer scheduleFixDelay;// int comment 'ms',
	private String sheduleCron;// varchar(20),

}

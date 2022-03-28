package io.github.icodegarden.beecomb.worker.pojo.transfer;

import java.time.LocalDateTime;

import io.github.icodegarden.commons.lang.annotation.NotNull;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class CreateJobExecuteRecordDTO {
	
	@NotNull
	private Long jobId;// bigint unsigned NOT NULL,
	@NotNull
	private LocalDateTime trigAt;// timestamp NOT NULL comment '任务调度触发时间',
	private String trigResult;// text comment '触发结果,例如没有可选的executor实例64K',
	private String executeExecutor;// varchar(21) comment 'ip:port',
	private String executeReturns;// varchar(200),
	@NotNull
	private Boolean success;// bit(1) NOT NULL,

}

package io.github.icodegarden.beecomb.common.backend.pojo.persistence;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class JobExecuteRecordPO {

	private Long id;// bigint unsigned NOT NULL,
	private Long jobId;// bigint unsigned NOT NULL,
	private String trigWorker;// varchar(21) comment 'ip:port',
	private LocalDateTime trigAt;// timestamp NOT NULL comment '任务调度触发时间',
	private String trigResult;// text comment '触发结果,例如没有可选的executor实例64K',
	private String executeExecutor;// varchar(21) comment 'ip:port',
	private String executeReturns;// varchar(200),
	private Boolean success;// bit(1) NOT NULL,
}

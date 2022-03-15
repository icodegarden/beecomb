package io.github.icodegarden.beecomb.master.pojo.view;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class JobRecoveryRecordVO {

	private Long jobId;// bigint unsigned NOT NULL,
	private Boolean success;// bit(1) NOT NULL,
	private String desc;// text comment '恢复结果描述65535',
	private LocalDateTime recoveryAt;// timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

	private JobVO job;
}

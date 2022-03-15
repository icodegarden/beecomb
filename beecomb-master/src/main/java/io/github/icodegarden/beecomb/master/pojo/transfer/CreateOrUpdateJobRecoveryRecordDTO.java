package io.github.icodegarden.beecomb.master.pojo.transfer;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class CreateOrUpdateJobRecoveryRecordDTO {

	private Long jobId;// bigint unsigned NOT NULL,
	private Boolean success;// bit(1) NOT NULL,
	private String desc;// text comment '恢复结果描述65535',
	private LocalDateTime recoveryAt;// timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	
}

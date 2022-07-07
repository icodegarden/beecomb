package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

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
public class CreatePendingRecoveryJobDTO {

	private Long jobId;
	private Integer priority;

}

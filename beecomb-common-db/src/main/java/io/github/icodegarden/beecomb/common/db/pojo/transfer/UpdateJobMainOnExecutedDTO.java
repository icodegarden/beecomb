package io.github.icodegarden.beecomb.common.db.pojo.transfer;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Builder
@Getter
@ToString
public class UpdateJobMainOnExecutedDTO {

	@NonNull
	private Long id;
	@NonNull
	private LocalDateTime lastTrigAt;
	/**
	 * 不一定有，如任务结束时
	 */
	private LocalDateTime nextTrigAt;
	@NonNull
	private String lastTrigResult;
	@NonNull
	private Boolean lastExecuteSuccess;
	/**
	 * 不一定有
	 */
	private String lastExecuteExecutor;
	/**
	 * 不一定有
	 */
	private String lastExecuteReturns;
	/**
	 * 不一定结束
	 */
	private Boolean end;

}

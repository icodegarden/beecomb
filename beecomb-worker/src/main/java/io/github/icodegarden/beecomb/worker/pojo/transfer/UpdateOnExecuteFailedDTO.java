package io.github.icodegarden.beecomb.worker.pojo.transfer;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Builder
@Data
public class UpdateOnExecuteFailedDTO {

	@NonNull
	private Long jobId;
	@NonNull
	private LocalDateTime lastTrigAt;
	@NonNull
	private Exception exception;
	/**
	 * 对delay忽略该字段<br>
	 * 对schedule不可以null<br>
	 */
	private LocalDateTime nextTrigAt;

}
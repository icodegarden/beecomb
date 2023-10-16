package io.github.icodegarden.beecomb.worker.pojo.transfer;

import java.time.LocalDateTime;

import io.github.icodegarden.nutrient.exchange.exception.NoQualifiedInstanceExchangeException;
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
public class UpdateOnNoQualifiedExecutorDTO {

	@NonNull
	private Long jobId;
	@NonNull
	private LocalDateTime lastTrigAt;
	@NonNull
	private NoQualifiedInstanceExchangeException noQualifiedInstanceExchangeException;
	/**
	 * 对delay忽略该字段<br>
	 * 对schedule不可以null<br>
	 */
	private LocalDateTime nextTrigAt;

}

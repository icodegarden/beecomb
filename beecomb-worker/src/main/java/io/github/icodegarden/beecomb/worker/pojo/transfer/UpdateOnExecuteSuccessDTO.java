package io.github.icodegarden.beecomb.worker.pojo.transfer;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import io.github.icodegarden.beecomb.worker.core.JobFreshParams;
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
public class UpdateOnExecuteSuccessDTO {
	
	@NonNull
	private Long jobId;
	@NonNull
	private LocalDateTime lastTrigAt;
	@NonNull
	private String executorIp;
	@NonNull
	private Integer executorPort;
	private String lastExecuteReturns;
	/**
	 * 对delay忽略该字段<br>
	 * 对schedule可以null<br>
	 */
	private Boolean end;
	/**
	 * 对delay忽略该字段<br>
	 * 对schedule不可以null<br>
	 */
	private LocalDateTime nextTrigAt;

	private Consumer<JobFreshParams> callback;
}

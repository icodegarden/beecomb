package io.github.icodegarden.beecomb.common.backend.pojo.query;

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
public class JobDetailQuery {
	
	@NonNull
	private Long jobId;

	private With with;

	@Builder
	@Getter
	@ToString
	public static class With {
		private boolean params;
		private boolean desc;
		private boolean lastTrigResult;
		private boolean lastExecuteReturns;
	}
}

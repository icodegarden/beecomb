package io.github.icodegarden.beecomb.common.db.pojo.query;

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
public class ScheduleJobQuery {

	@NonNull
	private Long jobId;

	private With with;

	@Builder
	@Getter
	@ToString
	public static class With {
	}
}

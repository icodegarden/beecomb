package io.github.icodegarden.beecomb.common.backend.pojo.query;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Fangfang.Xu
 *
 */
@Builder
@Getter
@ToString
public class JobMainCountQuery {

	private Boolean queued;
	private Boolean end;
	private Boolean lastExecuteSuccess;
	private LocalDateTime createdAtGte;
	private LocalDateTime createdAtLt;

	private GroupBy groupBy;

	@Builder
	public JobMainCountQuery(Boolean queued, Boolean end, Boolean lastExecuteSuccess, LocalDateTime createdAtGte,
			LocalDateTime createdAtLt, GroupBy groupBy) {
		super();
		this.queued = queued;
		this.end = end;
		this.lastExecuteSuccess = lastExecuteSuccess;
		this.createdAtGte = createdAtGte;
		this.createdAtLt = createdAtLt;
		this.groupBy = groupBy;
	}


	@Builder
	@Getter
	@ToString
	public static class GroupBy {
		private boolean createdBy;
		private boolean type;
	}

}

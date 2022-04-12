package io.github.icodegarden.beecomb.common.backend.pojo.query;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class JobExecuteRecordCountQuery {

	private Boolean queued;
	private Boolean end;
	private Boolean lastExecuteSuccess;

	private GroupBy groupBy;

	@Builder
	public JobExecuteRecordCountQuery(Boolean queued, Boolean end, Boolean lastExecuteSuccess, GroupBy groupBy) {
		super();
		this.queued = queued;
		this.end = end;
		this.lastExecuteSuccess = lastExecuteSuccess;
		this.groupBy = groupBy;
	}

	@Builder
	@Getter
	@ToString
	public static class GroupBy {
		private boolean createdBy;
		private boolean type;
		private boolean success;
	}
}
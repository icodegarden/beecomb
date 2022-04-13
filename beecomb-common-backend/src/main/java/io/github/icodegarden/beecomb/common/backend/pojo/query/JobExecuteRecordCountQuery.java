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

	private GroupBy groupBy;

	@Builder
	public JobExecuteRecordCountQuery(GroupBy groupBy) {
		super();
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
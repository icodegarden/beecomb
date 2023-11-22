package io.github.icodegarden.beecomb.common.backend.pojo.query;

import java.time.LocalDateTime;

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

	private LocalDateTime trigAtGte;
	private LocalDateTime trigAtLt;

	private GroupBy groupBy;

	@Builder
	public JobExecuteRecordCountQuery(LocalDateTime trigAtLt, LocalDateTime trigAtGte, GroupBy groupBy) {
		super();
		this.trigAtLt = trigAtLt;
		this.trigAtGte = trigAtGte;
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
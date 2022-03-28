package io.github.icodegarden.beecomb.common.db.pojo.query;

import io.github.icodegarden.beecomb.common.pojo.query.BaseQuery;
import lombok.Builder;
import lombok.Data;
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
public class JobExecuteRecordQuery extends BaseQuery {

	private Long jobId;
	private Boolean success;// bit(1) NOT NULL,

	private With with;

	@Builder
	public JobExecuteRecordQuery(int page, int size, String sort, String limit, Long jobId, Boolean success,
			With with) {
		super(page, size, sort, limit);
		this.jobId = jobId;
		this.success = success;
		this.with = with;
	}

	@Builder
	@Data
	public static class With {
		private boolean executeExecutor;
		private boolean executeReturns;
	}

}
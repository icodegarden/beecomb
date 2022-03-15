package io.github.icodegarden.beecomb.master.pojo.query;

import io.github.icodegarden.beecomb.common.pojo.query.BaseQuery;
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
public class JobRecoveryRecordQuery extends BaseQuery {

	private Long jobId;
	private Boolean success;// bit(1) NOT NULL,
	private String jobCreatedBy;
	private JobRecoveryRecordWith with;

	@Builder
	public JobRecoveryRecordQuery(int page, int size, String sort, String limit, Long jobId, Boolean success,
			String jobCreatedBy, JobRecoveryRecordWith with) {
		super(page, size, sort, limit);
		this.jobId = jobId;
		this.success = success;
		this.jobCreatedBy = jobCreatedBy;
		this.with = with;
	}

}
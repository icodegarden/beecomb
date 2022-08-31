package io.github.icodegarden.beecomb.master.pojo.query;

import io.github.icodegarden.commons.lang.query.BaseQuery;
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
public class JobRecoveryRecordQuery extends BaseQuery {

	private Long jobId;
	private Boolean success;// bit(1) NOT NULL,
	private String jobCreatedBy;

	private With with;

	@Builder
	public JobRecoveryRecordQuery(int page, int size, String orderBy, Long jobId, Boolean success,
			String jobCreatedBy, With with) {
		super(page, size, orderBy);
		this.jobId = jobId;
		this.success = success;
		this.jobCreatedBy = jobCreatedBy;
		this.with = with;
	}

	@Builder
	@Data
	public static class With {

		public static final With WITH_LEAST = With.builder().build();
		public static final With WITH_MOST = With.builder().desc(true).jobMain(JobMain.builder().build()).build();

		private boolean desc;
		private JobMain jobMain;

		@Builder
		@Data
		public static class JobMain {
		}
	}
}
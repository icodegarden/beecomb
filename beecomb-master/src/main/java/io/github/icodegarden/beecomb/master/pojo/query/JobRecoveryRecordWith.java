package io.github.icodegarden.beecomb.master.pojo.query;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Fangfang.Xu
 *
 */
@Builder
@Data
public class JobRecoveryRecordWith {

	public static final JobRecoveryRecordWith WITH_LEAST = JobRecoveryRecordWith.builder().build();
	public static final JobRecoveryRecordWith WITH_MOST = JobRecoveryRecordWith.builder().desc(true)
			.jobMain(JobMain.builder().build()).build();

	private boolean desc;
	private JobMain jobMain;

	@Builder
	@Data
	public static class JobMain {
	}
}
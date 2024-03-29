package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

import java.util.Arrays;

import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.Validateable;
import io.github.icodegarden.beecomb.common.constant.JobConstants;
import io.github.icodegarden.beecomb.common.util.ClassUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UpdateDelayJobDTO implements Validateable {

	@NotNull
	private Long jobId;

	private Long delay;
	private Integer retryOnExecuteFailed;
	private Integer retryBackoffOnExecuteFailed;
	private Integer retryOnNoQualified;
	private Integer retryBackoffOnNoQualified;

	@Override
	public void validate() throws IllegalArgumentException {
		Assert.notNull(jobId, "Missing:jobId");
		Assert.isTrue(delay >= JobConstants.MIN_EXECUTE_INTERVAL && delay <= JobConstants.MAX_EXECUTE_INTERVAL,
				"Invalid:delay");
	}

	@Override
	public boolean shouldUpdate() {
		return ClassUtils.anyFieldHasValue(this, Arrays.asList("jobId"));
	}
}

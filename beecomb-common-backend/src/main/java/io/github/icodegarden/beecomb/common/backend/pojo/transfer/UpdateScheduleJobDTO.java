package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

import java.util.Arrays;

import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.backend.validator.ScheduleValidator;
import io.github.icodegarden.beecomb.common.util.ClassUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Setter
@Getter
@ToString(callSuper = true)
public class UpdateScheduleJobDTO extends ScheduleValidator {

	@NotNull
	private Long jobId;

	private Long scheduleFixRate;
	private Long scheduleFixDelay;
	private String sheduleCron;

	@Override
	public void validate() throws IllegalArgumentException {
		Assert.notNull(jobId, "Missing:jobId");
		super.validate();
	}

	@Override
	public boolean shouldUpdate() {
		return ClassUtils.anyFieldHasValue(this, Arrays.asList("jobId"));
	}
}

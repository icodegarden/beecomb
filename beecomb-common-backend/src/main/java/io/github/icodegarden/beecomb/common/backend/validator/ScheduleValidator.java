package io.github.icodegarden.beecomb.common.backend.validator;

import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.Validateable;
import io.github.icodegarden.beecomb.common.constant.JobConstants;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.util.CronUtils;
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
@ToString
public class ScheduleValidator implements Validateable {

	private Long scheduleFixRate;
	private Long scheduleFixDelay;
	private String sheduleCron;

	@Override
	public void validate() throws IllegalArgumentException {
		int valid = 0;
		if (getScheduleFixDelay() != null) {
			Assert.isTrue(scheduleFixDelay >= JobConstants.MIN_EXECUTE_INTERVAL && scheduleFixDelay <= JobConstants.MAX_EXECUTE_INTERVAL,
					"Invalid:scheduleFixDelay");
			
			valid++;
		}
		if (getScheduleFixRate() != null) {
			Assert.isTrue(scheduleFixRate >= JobConstants.MIN_EXECUTE_INTERVAL && scheduleFixRate <= JobConstants.MAX_EXECUTE_INTERVAL,
					"Invalid:scheduleFixRate");
			
			valid++;
		}
		if (getSheduleCron() != null) {
			if (!CronUtils.isValid(getSheduleCron())) {
				throw new IllegalArgumentException("sheduleCron is not a valid expression");
			}

			valid++;
		}
		if (valid != 1) {
			throw new IllegalArgumentException(
					"must only one param should provide in [scheduleFixDelay,scheduleFixRate,sheduleCron]");
		}
	}
}

package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

import io.github.icodegarden.beecomb.common.Validateable;
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
public class CreateScheduleJobDTO implements Validateable {

	private Long jobId;
	private Integer scheduleFixRate;// int comment 'ms',
	private Integer scheduleFixDelay;// int comment 'ms',
	private String sheduleCron;// varchar(20),

	//FIXME 是不是不要throw ErrorCodeException，而是IllegalArgumentException
	@Override
	public void validate() throws ErrorCodeException {
		int valid = 0;
		if (getScheduleFixDelay() != null) {
			valid++;
		}
		if (getScheduleFixRate() != null) {
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

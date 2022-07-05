package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

import io.github.icodegarden.beecomb.common.backend.validator.ScheduleValidator;
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
public class CreateScheduleJobDTO extends ScheduleValidator {

	private Long jobId;
}

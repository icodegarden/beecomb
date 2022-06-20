package io.github.icodegarden.beecomb.common.backend.pojo.view;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.backend.pojo.data.ScheduleJobDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.ScheduleJobPO;
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
public class ScheduleJobVO extends ScheduleJobPO {

	public static ScheduleJobVO of(ScheduleJobDO one) {
		if (one == null) {
			return null;
		}
		ScheduleJobVO vo = new ScheduleJobVO();
		
		BeanUtils.copyProperties(one, vo);
		
		return vo;
	}
}
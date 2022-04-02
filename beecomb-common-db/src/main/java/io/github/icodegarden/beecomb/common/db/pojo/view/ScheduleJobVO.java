package io.github.icodegarden.beecomb.common.db.pojo.view;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.db.pojo.data.ScheduleJobDO;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class ScheduleJobVO {

	public static ScheduleJobVO of(ScheduleJobDO one) {
		if (one == null) {
			return null;
		}
		ScheduleJobVO vo = new ScheduleJobVO();
		
		BeanUtils.copyProperties(one, vo);
		
		return vo;
	}
}
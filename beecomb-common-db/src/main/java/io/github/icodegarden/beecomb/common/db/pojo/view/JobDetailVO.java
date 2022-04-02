package io.github.icodegarden.beecomb.common.db.pojo.view;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.db.pojo.data.JobDetailDO;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class JobDetailVO {

	public static JobDetailVO of(JobDetailDO one) {
		if (one == null) {
			return null;
		}
		JobDetailVO vo = new JobDetailVO();
		
		BeanUtils.copyProperties(one, vo);
		
		return vo;
	}
}
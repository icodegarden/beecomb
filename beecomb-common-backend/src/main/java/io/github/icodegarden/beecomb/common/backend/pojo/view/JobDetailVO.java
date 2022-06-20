package io.github.icodegarden.beecomb.common.backend.pojo.view;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.backend.pojo.data.JobDetailDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobDetailPO;
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
public class JobDetailVO extends JobDetailPO {
	
	public static JobDetailVO of(JobDetailDO one) {
		if (one == null) {
			return null;
		}
		JobDetailVO vo = new JobDetailVO();
		
		BeanUtils.copyProperties(one, vo);
		
		return vo;
	}
}
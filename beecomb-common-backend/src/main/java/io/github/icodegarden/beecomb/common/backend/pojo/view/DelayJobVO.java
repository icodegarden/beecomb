package io.github.icodegarden.beecomb.common.backend.pojo.view;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.backend.pojo.data.DelayJobDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.DelayJobPO;
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
public class DelayJobVO extends DelayJobPO {

	public static DelayJobVO of(DelayJobDO one) {
		if (one == null) {
			return null;
		}
		DelayJobVO vo = new DelayJobVO();
		
		BeanUtils.copyProperties(one, vo);
		
		return vo;
	}
}
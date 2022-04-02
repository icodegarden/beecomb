package io.github.icodegarden.beecomb.common.db.pojo.view;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.db.pojo.data.DelayJobDO;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class DelayJobVO {

	public static DelayJobVO of(DelayJobDO one) {
		if (one == null) {
			return null;
		}
		DelayJobVO vo = new DelayJobVO();
		
		BeanUtils.copyProperties(one, vo);
		
		return vo;
	}
}
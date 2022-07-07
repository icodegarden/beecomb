package io.github.icodegarden.beecomb.common.backend.pojo.view;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.backend.pojo.data.PendingRecoveryJobDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.PendingRecoveryJobPO;
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
public class PendingRecoveryJobVO extends PendingRecoveryJobPO {

	public static PendingRecoveryJobVO of(PendingRecoveryJobDO one) {
		if (one == null) {
			return null;
		}
		PendingRecoveryJobVO vo = new PendingRecoveryJobVO();

		BeanUtils.copyProperties(one, vo);

		return vo;
	}
}
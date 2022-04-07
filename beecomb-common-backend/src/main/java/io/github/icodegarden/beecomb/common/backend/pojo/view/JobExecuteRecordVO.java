package io.github.icodegarden.beecomb.common.backend.pojo.view;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.backend.pojo.data.JobExecuteRecordDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobExecuteRecordPO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class JobExecuteRecordVO extends JobExecuteRecordPO {

	public static JobExecuteRecordVO of(JobExecuteRecordDO one) {
		if (one == null) {
			return null;
		}
		JobExecuteRecordVO vo = new JobExecuteRecordVO();

		BeanUtils.copyProperties(one, vo);

		return vo;
	}
}

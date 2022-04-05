package io.github.icodegarden.beecomb.common.db.pojo.view;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.db.pojo.data.JobMainDO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
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
public class JobMainVO extends JobMainPO {

	private JobDetailVO jobDetail;
	private DelayJobVO delayJob;
	private ScheduleJobVO scheduleJob;

	public static JobMainVO of(JobMainDO one) {
		if (one == null) {
			return null;
		}
		JobMainVO vo = new JobMainVO();

		BeanUtils.copyProperties(one, vo);

		vo.setJobDetail(JobDetailVO.of(one.getJobDetail()));
		vo.setDelayJob(DelayJobVO.of(one.getDelayJob()));
		vo.setScheduleJob(ScheduleJobVO.of(one.getScheduleJob()));

		return vo;
	}
}
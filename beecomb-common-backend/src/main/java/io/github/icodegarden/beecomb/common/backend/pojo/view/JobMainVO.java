package io.github.icodegarden.beecomb.common.backend.pojo.view;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.icodegarden.beecomb.common.backend.pojo.data.JobMainDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ScheduleBO;
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

	@JsonIgnore
	public ExecutableJobBO toExecutableJob() {
		ExecutableJobBO executableJobBO = new ExecutableJobBO();
		BeanUtils.copyProperties(this, executableJobBO);

		if (jobDetail != null) {
			BeanUtils.copyProperties(jobDetail, executableJobBO);
		}

		if (delayJob != null) {
			DelayBO d = new DelayBO();
			BeanUtils.copyProperties(delayJob, d);

			executableJobBO.setDelay(d);
		}

		if (scheduleJob != null) {
			ScheduleBO s = new ScheduleBO();
			BeanUtils.copyProperties(scheduleJob, s);

			executableJobBO.setSchedule(s);
		}
		return executableJobBO;
	}
}
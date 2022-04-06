package io.github.icodegarden.beecomb.common.backend.pojo.data;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.backend.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobDetailPO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.ScheduleJobPO;
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
public class JobMainDO extends JobMainPO {

	private JobDetailDO jobDetail;
	private DelayJobDO delayJob;
	private ScheduleJobDO scheduleJob;

	public ExecutableJobBO toExecutableJobBO() {
		JobDetailPO jobDetail = getJobDetail();
		DelayJobPO delayJob = getDelayJob();
		ScheduleJobPO scheduleJob = getScheduleJob();

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

package io.github.icodegarden.beecomb.common.db.pojo.data;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.db.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobDetailPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.ScheduleJobPO;
import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ScheduleBO;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class JobDO {

	private JobMainPO jobMain;
	private JobDetailPO jobDetail;
	private DelayJobPO delayJob;
	private ScheduleJobPO scheduleJob;

	public ExecutableJobBO toExecutableJobBO() {
		JobMainPO jobMain = getJobMain();
		JobDetailPO jobDetail = getJobDetail();
		DelayJobPO delayJob = getDelayJob();
		ScheduleJobPO scheduleJob = getScheduleJob();

		ExecutableJobBO executableJobBO = new ExecutableJobBO();

		BeanUtils.copyProperties(jobMain, executableJobBO);
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

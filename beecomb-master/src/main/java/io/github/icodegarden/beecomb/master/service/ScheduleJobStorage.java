package io.github.icodegarden.beecomb.master.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.mapper.ScheduleJobMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.ScheduleJobPO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO.Schedule;
import io.github.icodegarden.commons.lang.util.CronUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service("schedule")
public class ScheduleJobStorage extends BaseJobStorage implements JobStorage {

	@Autowired
	private ScheduleJobMapper scheduleJobMapper;

	@Transactional
	@Override
	public ExecutableJobBO create(CreateJobDTO dto) throws IllegalArgumentException {
		Schedule schedule = dto.getSchedule();
		if (schedule == null) {
			throw new IllegalArgumentException("schedule must not null");
		}
		int valid = 0;
		if (schedule.getScheduleFixDelay() != null) {
			valid++;
		}
		if (schedule.getScheduleFixRate() != null) {
			valid++;
		}
		if (schedule.getSheduleCron() != null) {
			if (!CronUtils.isValid(schedule.getSheduleCron())) {
				throw new IllegalArgumentException("schedule.sheduleCron is not a valid expression");
			}

			valid++;
		}
		if (valid != 1) {
			throw new IllegalArgumentException(
					"must only one param should provide in [scheduleFixDelay,scheduleFixRate,sheduleCron]");
		}

		JobMainPO main = createBase(dto);

		ScheduleJobPO scheduleJobPO = new ScheduleJobPO();
		scheduleJobPO.setJobId(main.getId());
		scheduleJobPO.setScheduleFixDelay(schedule.getScheduleFixDelay());
		scheduleJobPO.setScheduleFixRate(schedule.getScheduleFixRate());
		scheduleJobPO.setSheduleCron(schedule.getSheduleCron());
		scheduleJobMapper.add(scheduleJobPO);

//		ScheduleBO s = new ScheduleBO();
//		s.setScheduledTimes(0L);
//		s.setScheduleFixDelay(scheduleJobPO.getScheduleFixDelay());
//		s.setScheduleFixRate(scheduleJobPO.getScheduleFixRate());
//		s.setSheduleCron(scheduleJobPO.getSheduleCron());
//		job.setSchedule(s);

		return findOneExecutableJob(main.getId());
	}

}

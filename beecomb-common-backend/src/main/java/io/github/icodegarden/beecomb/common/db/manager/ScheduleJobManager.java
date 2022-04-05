package io.github.icodegarden.beecomb.common.db.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.db.mapper.ScheduleJobMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.ScheduleJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.transfer.CreateScheduleJobDTO;
import io.github.icodegarden.commons.lang.util.CronUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class ScheduleJobManager {

	@Autowired
	private ScheduleJobMapper scheduleJobMapper;

	public void create(CreateScheduleJobDTO dto) throws IllegalArgumentException {
		int valid = 0;
		if (dto.getScheduleFixDelay() != null) {
			valid++;
		}
		if (dto.getScheduleFixRate() != null) {
			valid++;
		}
		if (dto.getSheduleCron() != null) {
			if (!CronUtils.isValid(dto.getSheduleCron())) {
				throw new IllegalArgumentException("sheduleCron is not a valid expression");
			}

			valid++;
		}
		if (valid != 1) {
			throw new IllegalArgumentException(
					"must only one param should provide in [scheduleFixDelay,scheduleFixRate,sheduleCron]");
		}

		ScheduleJobPO po = new ScheduleJobPO();
		BeanUtils.copyProperties(dto, po);

		scheduleJobMapper.add(po);
	}

}

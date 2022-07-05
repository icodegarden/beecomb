package io.github.icodegarden.beecomb.common.backend.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.backend.mapper.ScheduleJobMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.data.ScheduleJobDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.ScheduleJobPO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.ScheduleJobPO.Update;
import io.github.icodegarden.beecomb.common.backend.pojo.query.ScheduleJobQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateScheduleJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateScheduleJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.ScheduleJobVO;
import io.github.icodegarden.commons.springboot.exception.SQLConstraintException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class ScheduleJobManager {

	@Autowired
	private ScheduleJobMapper scheduleJobMapper;

	public void create(CreateScheduleJobDTO dto) {
		dto.validate();

		ScheduleJobPO po = new ScheduleJobPO();
		BeanUtils.copyProperties(dto, po);

		try {
			scheduleJobMapper.add(po);
		} catch (DataIntegrityViolationException e) {
			throw new SQLConstraintException(e);
		}
	}

	public ScheduleJobVO findOne(Long jobId, @Nullable ScheduleJobQuery.With with) {
		ScheduleJobDO one = scheduleJobMapper.findOne(jobId, with);
		return ScheduleJobVO.of(one);
	}
	
	public boolean update(UpdateScheduleJobDTO dto) {
		dto.validate();
		
		Update update = new ScheduleJobPO.Update();
		BeanUtils.copyProperties(dto, update);
		
		return scheduleJobMapper.updateAlways(update) == 1;
	}
	
	public boolean delete(Long jobId) {
		return scheduleJobMapper.delete(jobId) == 1;
	}
}

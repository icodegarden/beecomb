package io.github.icodegarden.beecomb.common.db.mapper;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.common.db.pojo.persistence.ScheduleJobPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ScheduleJobMapper {

	void add(ScheduleJobPO po);
	
	ScheduleJobPO findOne(@Param("jobId") Long jobId);
	
	int update(ScheduleJobPO.Update update);
	
	int updateAndIncrementScheduledTimes(ScheduleJobPO.Update update);
}

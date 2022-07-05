package io.github.icodegarden.beecomb.common.backend.mapper;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.common.backend.pojo.data.ScheduleJobDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.ScheduleJobPO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.ScheduleJobQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ScheduleJobMapper {

	void add(ScheduleJobPO po);

	ScheduleJobDO findOne(@Param("jobId") Long jobId, @Param("with") ScheduleJobQuery.With with);

	int update(ScheduleJobPO.Update update);
	
	/**
	 * 始终更新所有字段，null也会
	 * @param update
	 * @return
	 */
	int updateAlways(ScheduleJobPO.Update update);

	int updateAndIncrementScheduledTimes(ScheduleJobPO.Update update);
	
	int delete(@Param("jobId") Long jobId);
}

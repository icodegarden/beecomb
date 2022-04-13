package io.github.icodegarden.beecomb.common.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.common.backend.pojo.data.JobExecuteRecordCountDO;
import io.github.icodegarden.beecomb.common.backend.pojo.data.JobExecuteRecordDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobExecuteRecordPO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobExecuteRecordCountQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobExecuteRecordQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobExecuteRecordMapper {

	void add(JobExecuteRecordPO po);

	JobExecuteRecordDO findOne(@Param("id") Long id, @Param("with") JobExecuteRecordQuery.With with);

	List<JobExecuteRecordDO> findAll(JobExecuteRecordQuery query);
	
	List<JobExecuteRecordCountDO> count(JobExecuteRecordCountQuery query);
}

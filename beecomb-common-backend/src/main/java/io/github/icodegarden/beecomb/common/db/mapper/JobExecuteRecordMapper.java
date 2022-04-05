package io.github.icodegarden.beecomb.common.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.common.db.pojo.data.JobExecuteRecordDO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobExecuteRecordPO;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobExecuteRecordQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobExecuteRecordMapper {

	void add(JobExecuteRecordPO po);

	JobExecuteRecordDO findOne(@Param("id") Long id, @Param("with") JobExecuteRecordQuery.With with);

	List<JobExecuteRecordDO> findAll(JobExecuteRecordQuery query);
}

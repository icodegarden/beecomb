package io.github.icodegarden.beecomb.master.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.master.pojo.data.JobRecoveryRecordDO;
import io.github.icodegarden.beecomb.master.pojo.persistence.JobRecoveryRecordPO;
import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobRecoveryRecordMapper {

	void addOrUpdate(JobRecoveryRecordPO po);

	JobRecoveryRecordDO findOne(@Param("jobId") Long jobId, @Param("with") JobRecoveryRecordQuery.With with);

	List<JobRecoveryRecordDO> findAll(JobRecoveryRecordQuery query);
}

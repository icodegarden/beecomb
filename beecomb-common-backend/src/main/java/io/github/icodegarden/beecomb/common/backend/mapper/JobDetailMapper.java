package io.github.icodegarden.beecomb.common.backend.mapper;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.common.backend.pojo.data.JobDetailDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobDetailPO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobDetailQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobDetailMapper {

	void add(JobDetailPO po);

	JobDetailDO findOne(@Param("jobId") Long jobId, @Param("with") JobDetailQuery.With with);

	int update(JobDetailPO.Update update);
	
	int delete(@Param("jobId") Long jobId);
}

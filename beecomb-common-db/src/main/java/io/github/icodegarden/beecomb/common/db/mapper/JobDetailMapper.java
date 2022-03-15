package io.github.icodegarden.beecomb.common.db.mapper;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobDetailPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobDetailMapper {

	void add(JobDetailPO po);

	JobDetailPO findOne(@Param("jobId") Long jobId);

	void update(JobDetailPO.Update update);
}

package io.github.icodegarden.beecomb.common.backend.mapper;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.common.backend.pojo.data.DelayJobDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.DelayJobQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface DelayJobMapper {

	void add(DelayJobPO po);

	DelayJobDO findOne(@Param("jobId") Long jobId, @Param("with") DelayJobQuery.With with);

	int update(DelayJobPO.Update update);
}

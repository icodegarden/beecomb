package io.github.icodegarden.beecomb.common.db.mapper;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.common.db.pojo.data.DelayJobDO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.DelayJobPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface DelayJobMapper {

	void add(DelayJobPO po);
	
	DelayJobDO findOne(@Param("jobId") Long jobId);
	
	int update(DelayJobPO.Update update);
}

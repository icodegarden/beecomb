package io.github.icodegarden.beecomb.common.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.common.backend.pojo.data.PendingRecoveryJobDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.PendingRecoveryJobPO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.PendingRecoveryJobQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface PendingRecoveryJobMapper {

	void add(PendingRecoveryJobPO po);

	int insertSelectByScan(PendingRecoveryJobPO.InsertSelect obj);

	int insertSelectByInstance(PendingRecoveryJobPO.InsertSelect obj);

	List<PendingRecoveryJobDO> findAll(PendingRecoveryJobQuery query);

	List<PendingRecoveryJobDO> listJobsShouldRecovery(@Param("skip") int skip, @Param("size") int size,
			@Param("with") PendingRecoveryJobQuery.With with);

	int delete(@Param("jobId") Long jobId);
}

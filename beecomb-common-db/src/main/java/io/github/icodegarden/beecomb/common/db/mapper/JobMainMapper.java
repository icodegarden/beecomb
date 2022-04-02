package io.github.icodegarden.beecomb.common.db.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.common.db.pojo.data.JobMainDO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobMainQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobMainMapper {

	void add(JobMainPO po);

	List<JobMainDO> findAll(JobMainQuery query);

	/**
	 * 
	 * @param id   NotNull
	 * @param with Nullable
	 * @return
	 */
	JobMainDO findOne(@Param("id") Long id, @Param("with") JobMainQuery.With with);

	/**
	 * 
	 * @param uuid NotNull
	 * @param with Nullable
	 * @return
	 */
	JobMainDO findByUUID(@Param("uuid") String uuid, @Param("with") JobMainQuery.With with);

	int update(JobMainPO.Update update);

	void delete(@Param("id") Long id);

	int updateToNoQueued(@Param("nextTrigAtLt") LocalDateTime nextTrigAtLt);
}

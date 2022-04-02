package io.github.icodegarden.beecomb.common.db.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.common.db.pojo.data.JobDO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobMainMapper {

	void add(JobMainPO po);

	List<JobDO> findAll(JobQuery query);

	/**
	 * 
	 * @param id   NotNull
	 * @param with Nullable
	 * @return
	 */
	JobDO findOne(@Param("id") Long id, @Param("with") JobQuery.With with);

	/**
	 * 
	 * @param uuid NotNull
	 * @param with Nullable
	 * @return
	 */
	JobDO findByUUID(@Param("uuid") String uuid, @Param("with") JobQuery.With with);

	int update(JobMainPO.Update update);

	void delete(@Param("id") Long id);

	int updateToNoQueued(@Param("nextTrigAtLt") LocalDateTime nextTrigAtLt);
}

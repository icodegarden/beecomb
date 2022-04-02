package io.github.icodegarden.beecomb.master.manager;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.db.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.view.JobVO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobManager {

	ExecutableJobBO create(CreateJobDTO dto) throws IllegalArgumentException;

	Page<JobVO> page(JobMainQuery query);

	JobVO findOne(Long id, JobMainQuery.With with);

	JobVO findByUUID(String uuid, JobMainQuery.With with);

	ExecutableJobBO findOneExecutableJob(Long id);
}

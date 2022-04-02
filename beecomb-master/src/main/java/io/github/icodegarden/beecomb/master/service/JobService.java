package io.github.icodegarden.beecomb.master.service;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.db.pojo.query.JobQuery;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.view.JobVO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobService {

	ExecutableJobBO create(CreateJobDTO dto) throws IllegalArgumentException;

	Page<JobVO> page(JobQuery query);

	JobVO findOne(Long id, JobQuery.With with);

	JobVO findByUUID(String uuid, JobQuery.With with);

	ExecutableJobBO findOneExecutableJob(Long id);
}

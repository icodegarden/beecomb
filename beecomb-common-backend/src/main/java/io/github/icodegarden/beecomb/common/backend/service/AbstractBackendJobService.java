package io.github.icodegarden.beecomb.common.backend.service;

import org.springframework.beans.factory.annotation.Autowired;

import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AbstractBackendJobService implements BackendJobService {

	@Autowired
	protected JobMainManager jobMainManager;

	@Override
	public ExecutableJobBO findOneExecutableJob(Long id) {
		JobMainVO vo = jobMainManager.findOne(id, JobMainQuery.With.WITH_EXECUTABLE);
		if (vo == null) {
			return null;
		}
		return vo.toExecutableJob();
	}

}

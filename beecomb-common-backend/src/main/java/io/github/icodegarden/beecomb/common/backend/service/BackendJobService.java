package io.github.icodegarden.beecomb.common.backend.service;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface BackendJobService {

	/**
	 * FIXME 返回体确定要这样吗
	 */
	public ExecutableJobBO findOneExecutableJob(Long id);

}

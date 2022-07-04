package io.github.icodegarden.beecomb.common.backend.service;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface BackendJobService {

	public ExecutableJobBO findOneExecutableJob(Long id);

}

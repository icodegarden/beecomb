package io.github.icodegarden.beecomb.master.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.commons.lang.spec.response.ClientBizErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobLocalService {

	@Autowired
	private JobFacadeManager jobFacadeManager;
	@Autowired
	private JobRemoteService jobRemoteService;

	/**
	 * 删除任务
	 * 
	 * @param jobId
	 */
	public boolean delete(ExecutableJobBO job) throws ErrorCodeException {
		if (job == null) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.NOT_FOUND, "job not found");
		}

		if (job.getEnd()) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN, "job was end");
		}

		boolean remove = jobRemoteService.removeQueue(job);
		if (remove) {
			/**
			 * 处理数据库要在远程执行后，因为任务可能正在执行中需要数据
			 */
			jobFacadeManager.delete(job.getId());
		}
		return remove;
	}
}

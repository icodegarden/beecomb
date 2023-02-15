package io.github.icodegarden.beecomb.master.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdateJobDTO;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.lang.spec.response.ClientBizErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Service
public class JobLocalService {

	@Autowired
	private JobFacadeManager jobFacadeManager;
	@Autowired
	private WorkerRemoteService jobRemoteService;

	public boolean updateByApi(UpdateJobDTO dto) throws ErrorCodeException {
		ExecutableJobBO job = jobFacadeManager.findOneExecutableJob(dto.getId());

		if (job == null) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.NOT_FOUND, "Job Not Found");
		}
		/**
		 * 校验归属权
		 */
		if (!SecurityUtils.getUsername().equals(job.getCreatedBy())) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN, "Ownership");
		}

		boolean doRemoved = false;
		/**
		 * 如果处于队列中需要先移除
		 */
		if (removeQueueRequiredByUpdateParams(dto, job) && job.getQueued()) {
			boolean remove;
			try {
				remove = jobRemoteService.removeQueue(job);
			} catch (ExchangeException e) {
				throw new ServerErrorCodeException("updateJob", e.getMessage(), e);
			}
			if (!remove) {
				throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN,
						"job remove queue failed");
			}
			doRemoved = true;
		}
		/**
		 * 事务从这里开始，避免了远程执行增加事务时间
		 */
		boolean update = jobFacadeManager.update(dto, doRemoved);
		/**
		 * 移除后需要重新进
		 */
		if (doRemoved) {
			try {
				ExecutableJobBO executableJobBO = jobFacadeManager.findOneExecutableJob(dto.getId());
				jobRemoteService.enQueue(executableJobBO);
			} catch (Exception e) {
				/**
				 * 失败不影响更新
				 */
				log.error("WARN ex on enQueue when update job after removeQueue, job.id:{}, job.name:{}", job.getId(),
						job.getName(), e);
			}
		}

		return update;
	}

	/**
	 * 删除任务
	 * 
	 * @param jobId
	 */
	public boolean delete(Long id) throws ErrorCodeException {
		ExecutableJobBO job = jobFacadeManager.findOneExecutableJob(id);

		if (job == null) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.NOT_FOUND, "Job Not Found");
		}
		/**
		 * 校验归属权
		 */
		if (!SecurityUtils.getUsername().equals(job.getCreatedBy())) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN, "Ownership");
		}

		if (job.getEnd()) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN, "Job Was End");
		}

		try {
			boolean remove = jobRemoteService.removeQueue(job);
			if (remove) {
				/**
				 * 处理数据库要在远程执行后，因为任务可能正在执行中需要数据
				 */
				jobFacadeManager.delete(job.getId());
			}
			return remove;
		} catch (ExchangeException e) {
			throw new ServerErrorCodeException("deleteJob", e.getMessage(), e);
		}
	}

	private boolean removeQueueRequiredByUpdateParams(UpdateJobDTO dto, ExecutableJobBO job) {
		/**
		 * 这些参数有变要先移除队列
		 */
		if (dto.getDelay() != null && job.getDelay() != null) {
			if (!Objects.equals(dto.getDelay().getDelay(), job.getDelay().getDelay())) {
				return true;
			}
		} else if (dto.getSchedule() != null && job.getSchedule() != null) {
			if (!Objects.equals(dto.getSchedule().getScheduleFixDelay(), job.getSchedule().getScheduleFixDelay())) {
				return true;
			}
			if (!Objects.equals(dto.getSchedule().getScheduleFixRate(), job.getSchedule().getScheduleFixRate())) {
				return true;
			}
			if (!Objects.equals(dto.getSchedule().getSheduleCron(), job.getSchedule().getSheduleCron())) {
				return true;
			}
		}
		return false;
	}
}

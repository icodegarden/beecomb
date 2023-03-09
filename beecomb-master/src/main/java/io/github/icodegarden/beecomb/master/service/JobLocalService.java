package io.github.icodegarden.beecomb.master.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.enums.JobType;
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

	/**
	 * 任务被api更新<br>
	 */
	public boolean updateByApi(UpdateJobDTO dto) throws ErrorCodeException {
		ExecutableJobBO job = getValidate(dto.getId());

		boolean removed = false;
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
			removed = true;
		}
		/**
		 * 事务从这里开始，避免了远程执行增加事务时间和这条数据的死锁
		 */
		boolean update = jobFacadeManager.update(dto, removed);
		/**
		 * 移除后需要重新进
		 */
		if (removed) {
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
	 * 任务重进队列<br>
	 */
	public void reEnQueue(Long id) throws ErrorCodeException {
		ExecutableJobBO job = getValidate(id);

		/**
		 * 如果处于队列中需要先移除
		 */
		if (job.getQueued()) {
			boolean remove;
			try {
				remove = jobRemoteService.removeQueue(job);
			} catch (ExchangeException e) {
				throw new ServerErrorCodeException("remove on reEnQueue", e.getMessage(), e);
			}
			if (!remove) {
				throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN,
						"job remove queue failed");
			}

			UpdateJobDTO dto = new UpdateJobDTO();
			dto.setId(id);
			/**
			 * 事务从这里开始，避免了远程执行增加事务时间和这条数据的死锁
			 */
			jobFacadeManager.update(dto, true);
		}

		/**
		 * 重新进
		 */
		try {
			jobRemoteService.enQueue(job);
		} catch (ExchangeException e) {
			throw new ServerErrorCodeException("enQueue on reEnQueue", e.getMessage(), e);
		}
	}

	/**
	 * 任务立即执行<br>
	 */
	public void runJob(Long id) throws ErrorCodeException {
		ExecutableJobBO job = getValidate(id);

		/**
		 * 只允许schedule类型的
		 */
		if (!JobType.Schedule.equals(job.getType())) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN,
					"Job Type Not Support");
		}

		/**
		 * 必须已进队列
		 */
		if (!job.getQueued()) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN, "Job Not Queued");
		}

		try {
			jobRemoteService.runJob(job);
		} catch (ExchangeException e) {
			throw new ServerErrorCodeException("runJob", e.getMessage(), e);
		}
	}

	/**
	 * 删除任务
	 * 
	 * @param jobId
	 */
	public boolean delete(Long id) throws ErrorCodeException {
		ExecutableJobBO job = getValidate(id);

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

	private ExecutableJobBO getValidate(Long id) {
		ExecutableJobBO job = jobFacadeManager.findOneExecutableJob(id);

		if (job == null) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.NOT_FOUND, "Job Not Found");
		}
		/**
		 * 校验归属权
		 */
		if (!SecurityUtils.getUsername().equals(job.getCreatedBy())) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN, "Job Ownership");
		}

		if (job.getEnd()) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN, "Job Was End");
		}

		return job;
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

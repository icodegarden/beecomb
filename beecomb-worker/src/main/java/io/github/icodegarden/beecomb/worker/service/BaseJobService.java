package io.github.icodegarden.beecomb.worker.service;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.manager.PendingRecoveryJobManager;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobDetailDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobMainEnQueueDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobMainOnExecutedDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobOnExecutedDTO;
import io.github.icodegarden.beecomb.common.backend.service.AbstractBackendJobService;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.nutrient.exchange.exception.ExchangeException;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class BaseJobService extends AbstractBackendJobService implements JobService {

	protected static final RetryTemplate RETRY_TEMPLATE = RetryTemplate.builder().fixedBackoff(1000).maxAttempts(3)
			.retryOn(Exception.class).build();

	protected static final String IP = SystemUtils.getIp();

	@Autowired
	protected JobMainManager jobMainManager;
	@Autowired
	private PendingRecoveryJobManager pendingRecoveryJobManager;
	@Autowired
	protected InstanceProperties instanceProperties;

	@Transactional
	@Override
	public void updateEnQueue(ExecutableJobBO job) {
		Long jobId = job.getId();
		/**
		 * 进队列时计算出下次触发时间
		 */
		LocalDateTime nextTrigAt = job.calcNextTrigAtOnEnQueue();

		String queuedAtInstance = SystemUtils.formatIpPort(instanceProperties.getServer().getBindIp(),
				instanceProperties.getServer().getPort());
		UpdateJobMainEnQueueDTO update = UpdateJobMainEnQueueDTO.builder().id(jobId).queuedAtInstance(queuedAtInstance)
				.nextTrigAt(nextTrigAt).build();

		jobMainManager.updateEnQueue(update);
		/**
		 * 同时删除 待恢复数据
		 */
		pendingRecoveryJobManager.delete(job.getId());
	}
	
	@Transactional
	public boolean update(UpdateJobOnExecutedDTO dto) {
		boolean update = false;

		UpdateJobMainOnExecutedDTO updateJobMainDTO = new UpdateJobMainOnExecutedDTO();
		BeanUtils.copyProperties(dto, updateJobMainDTO);
		if (updateJobMainDTO.shouldUpdate()) {
			update = jobMainManager.updateOnExecuted(updateJobMainDTO);
		}

		UpdateJobDetailDTO updateJobDetailDTO = new UpdateJobDetailDTO();
		BeanUtils.copyProperties(dto, updateJobDetailDTO);
		updateJobDetailDTO.setJobId(dto.getId());
		if (updateJobDetailDTO.shouldUpdate()) {
			update = jobDetailManager.update(updateJobDetailDTO);
		}

		return update;
	}

	protected String buildLastTrigResult(ExchangeException e) {
		return e.getMessage();
	}

	protected String buildLastTrigResult(Exception exception) {
		String lastTrigResult = exception.getMessage();
		if (lastTrigResult == null) {
			return exception.getClass().getName();
		}
		return lastTrigResult;
	}

}

package io.github.icodegarden.beecomb.worker.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;

import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO.Update;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class BaseJobStorage implements JobStorage {
	protected static final RetryTemplate RETRY_TEMPLATE = RetryTemplate.builder().fixedBackoff(1000).maxAttempts(3)
			.retryOn(Exception.class).build();

	protected static final String IP = SystemUtils.getIp();

	@Autowired
	protected JobMainMapper jobMainMapper;
	@Autowired
	protected InstanceProperties instanceProperties;

	@Override
	public void updateEnQueue(ExecutableJobBO job) {
		Long jobId = job.getId();
		/**
		 * 进队列时计算出下次触发时间
		 */
		LocalDateTime nextTrigAt = job.calcNextTrigAtOnEnQueue();

		String queuedAtInstance = SystemUtils.formatIpPort(instanceProperties.getServer().getBindIp(),
				instanceProperties.getServer().getPort());
		Update update = JobMainPO.Update.builder().id(jobId).queued(true).queuedAt(SystemUtils.now())
				.queuedAtInstance(queuedAtInstance).nextTrigAt(nextTrigAt).build();
		jobMainMapper.update(update);
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

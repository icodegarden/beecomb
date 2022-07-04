package io.github.icodegarden.beecomb.worker.server;

import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.worker.core.JobEngine;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.beecomb.worker.exception.WorkerException;
import io.github.icodegarden.beecomb.worker.service.JobService;
import io.github.icodegarden.commons.lang.result.Result3;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class JobReceiver {

	private JobService jobService;
	private JobEngine jobEngine;

	public JobReceiver(JobService jobService, JobEngine jobEngine) {
		this.jobService = jobService;
		this.jobEngine = jobEngine;
	}

	/**
	 * 
	 * @param job
	 * @throws 失败并回滚事务，原因可见message
	 */
	@Transactional
	public void receive(ExecutableJobBO job) throws WorkerException {
		if (log.isDebugEnabled()) {
			log.debug("receive job:{}", job);
		}

		boolean allowEnQueue = jobEngine.allowEnQueue(job);
		if (!allowEnQueue) {
			if (log.isWarnEnabled()) {
				log.warn("job was rejected on receive, Exceed Overload");
			}
			throw new WorkerException("Exceed Overload");
		}

		try {
			jobService.updateEnQueue(job);

			Result3<ExecutableJobBO, ? extends Object, JobEngineException> enQueueResult = jobEngine.enQueue(job);
			if (!enQueueResult.isSuccess()) {
				/**
				 * 回滚事务
				 */
				throw enQueueResult.getT3();
			}
		} catch (Exception e) {
			throw new WorkerException("ex on job en queue", e);
		}
	}

}

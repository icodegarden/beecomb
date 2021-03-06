package io.github.icodegarden.beecomb.worker.server;

import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.view.RemoveQueueVO;
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
public class JobRequestReceiver {

	private JobService jobService;
	private JobEngine jobEngine;

	public JobRequestReceiver(JobService jobService, JobEngine jobEngine) {
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

	public RemoveQueueVO remove(ExecutableJobBO job) {
		boolean remove = jobEngine.removeQueue(job);
		if(remove) {
			/**
			 * 这个更新放在master处理，这样可以优化性能。因为删除任务和更新任务都需要走这里，而删除任务就不需要执行update
			 */
//			jobService.updateRemoveQueue(job);
		}

		return new RemoveQueueVO(job.getId(), remove);
	}
}

package io.github.icodegarden.beecomb.worker.server;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.view.RemoveQueueVO;
import io.github.icodegarden.beecomb.worker.core.JobEngine;
import io.github.icodegarden.beecomb.worker.exception.WorkerException;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class DispatcherHandler {

	private JobReceiver jobReceiver;
	private JobEngine jobEngine;

	public DispatcherHandler(JobReceiver jobReceiver, JobEngine jobEngine) {
		this.jobReceiver = jobReceiver;
		this.jobEngine = jobEngine;
	}

	public void receiveJob(ExecutableJobBO job) throws WorkerException {
		jobReceiver.receive(job);
	}

	public RemoveQueueVO removeJob(ExecutableJobBO job) {
		boolean remove = jobEngine.removeQueue(job);

		return new RemoveQueueVO(job.getId(), remove);
	}

}

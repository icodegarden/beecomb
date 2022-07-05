package io.github.icodegarden.beecomb.worker.server;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.view.RemoveQueueVO;
import io.github.icodegarden.beecomb.worker.exception.WorkerException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DispatcherHandler {

	private JobRequestReceiver jobRequestReceiver;

	public DispatcherHandler(JobRequestReceiver jobRequestReceiver) {
		this.jobRequestReceiver = jobRequestReceiver;
	}

	public void receiveJob(ExecutableJobBO job) throws WorkerException {
		jobRequestReceiver.receive(job);
	}

	public RemoveQueueVO removeJob(ExecutableJobBO job) {
		return jobRequestReceiver.remove(job);
	}

}

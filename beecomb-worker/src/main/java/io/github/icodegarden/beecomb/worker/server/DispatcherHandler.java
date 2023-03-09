package io.github.icodegarden.beecomb.worker.server;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.view.RemoveQueueVO;
import io.github.icodegarden.beecomb.common.pojo.view.RunJobVO;
import io.github.icodegarden.beecomb.worker.core.JobEngine;
import io.github.icodegarden.beecomb.worker.exception.WorkerException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DispatcherHandler {

	private JobRequestReceiver jobRequestReceiver;
	private JobEngine jobEngine;

	public DispatcherHandler(JobRequestReceiver jobRequestReceiver, JobEngine jobEngine) {
		this.jobRequestReceiver = jobRequestReceiver;
		this.jobEngine = jobEngine;
	}
	
	public String ping() {
		return "pong";
	}
	
	public void receiveJob(ExecutableJobBO job) throws WorkerException {
		jobRequestReceiver.receive(job);
	}

	public RemoveQueueVO removeJob(ExecutableJobBO job) {
		return jobRequestReceiver.remove(job);
	}
	
	public RunJobVO runJob(ExecutableJobBO job) {
		return jobRequestReceiver.run(job);
	}
	
	public int queuedSize() {
		return jobEngine.queuedSize();
	}
	
}

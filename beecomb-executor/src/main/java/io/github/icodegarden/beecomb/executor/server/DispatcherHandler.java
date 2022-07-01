package io.github.icodegarden.beecomb.executor.server;

import io.github.icodegarden.beecomb.common.executor.DelayJob;
import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.common.executor.ScheduleJob;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.lang.result.Result2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DispatcherHandler {

	private JobReceiver jobReceiver;

	public DispatcherHandler(JobReceiver jobReceiver) {
		this.jobReceiver = jobReceiver;
	}
	
	/**
	 * 具体参数化 
	 */
	public Result2<ExecuteJobResult, ExchangeFailedReason> receiveJob(Job job) {
		return jobReceiver.receive(job);
	}
	/**
	 * 具体参数化 
	 */
	public Result2<ExecuteJobResult, ExchangeFailedReason> receiveJob(DelayJob job) {
		return jobReceiver.receive(job);
	}
	/**
	 * 具体参数化 
	 */
	public Result2<ExecuteJobResult, ExchangeFailedReason> receiveJob(ScheduleJob job) {
		return jobReceiver.receive(job);
	}

}
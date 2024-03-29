package io.github.icodegarden.beecomb.executor.server;

import io.github.icodegarden.beecomb.common.executor.DelayJob;
import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.common.executor.ScheduleJob;
import io.github.icodegarden.nutrient.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.nutrient.lang.result.Result2;

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
	
	public String ping() {
		return "pong";
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

	/**
	 * 具体参数化 
	 */
	public Result2<Object, ExchangeFailedReason> onParallelSuccess(Job job) {
		return jobReceiver.onParallelSuccess(job);
	}
	/**
	 * 具体参数化 
	 */
	public Result2<Object, ExchangeFailedReason> onParallelSuccess(DelayJob job) {
		return jobReceiver.onParallelSuccess(job);
	}
	/**
	 * 具体参数化 
	 */
	public Result2<Object, ExchangeFailedReason> onParallelSuccess(ScheduleJob job) {
		return jobReceiver.onParallelSuccess(job);
	}
}
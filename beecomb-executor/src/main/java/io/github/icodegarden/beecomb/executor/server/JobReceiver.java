package io.github.icodegarden.beecomb.executor.server;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.beecomb.executor.registry.JobHandlerRegistry;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.lang.metricsregistry.MetricsOverload;
import io.github.icodegarden.commons.lang.result.Result2;
import io.github.icodegarden.commons.lang.result.Results;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JobReceiver {
	private static final Logger log = LoggerFactory.getLogger(JobReceiver.class);

	private final JobHandlerRegistry jobHandlerRegistry;
	private final MetricsOverload jobOverload;

	public JobReceiver(JobHandlerRegistry jobHandlerRegistry, MetricsOverload jobOverload) {
		this.jobHandlerRegistry = jobHandlerRegistry;
		this.jobOverload = jobOverload;
	}

	/**
	 * 不会抛出异常
	 * 
	 * @param job
	 * @return ExecuteJobResult:on success,ExchangeFailedReason:on fail
	 */
	public Result2<ExecuteJobResult, ExchangeFailedReason> receive(Job job) {
		return handleJobBased(job, jobHandler->{
			ExecuteJobResult result = jobHandler.handle(job);
			if (result == null) {
				/**
				 * build a result when user return null
				 */
				result = new ExecuteJobResult();
			}
			return result;
		});
	}
	
	private interface HandleFunc<R>{
		R apply(JobHandler jobHandler)throws Exception;
	}
	
//	private class JobBasedDTO{
//		private final Job job;
//		private final JobHandler jobHandler;
//		public JobBasedDTO(Job job, JobHandler jobHandler) {
//			this.job = job;
//			this.jobHandler = jobHandler;
//		}
//		public Job getJob() {
//			return job;
//		}
//		public JobHandler getJobHandler() {
//			return jobHandler;
//		}
//	}
	
	private <R>Result2<R, ExchangeFailedReason> handleJobBased(Job job,HandleFunc<R> func) {
		JobHandler jobHandler = jobHandlerRegistry.getJobHandler(job.getJobHandlerName());
		if (jobHandler == null) {
			return Results.of(false, null, ExchangeFailedReason.serverRejected("No JobHandler", null));
		}
		boolean b = jobOverload.incrementOverload(job);
		if (!b) {
			return Results.of(false, null, ExchangeFailedReason.serverRejected("Exceed Overload", null));
		}

		try {
			R result = func.apply(jobHandler);
			return Results.of(true, result, null);
		} catch (Exception e) {
			log.error("handle job failed, job:{}, jobHandler.name:{}", job, jobHandler.name(), e);
			String desc = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
			return Results.of(false, null, ExchangeFailedReason.serverException(desc, e));
		} finally {
			jobOverload.decrementOverload(job);
		}
	}

	/**
	 * 不会抛出异常
	 * 
	 * @param job
	 * @return ExecuteJobResult:on success,ExchangeFailedReason:on fail
	 */
	public Result2<Object, ExchangeFailedReason> onParallelSuccess(Job job) {
		return handleJobBased(job, jobHandler->{
			jobHandler.onParallelSuccess(job);
			return null;
		});
	}
}

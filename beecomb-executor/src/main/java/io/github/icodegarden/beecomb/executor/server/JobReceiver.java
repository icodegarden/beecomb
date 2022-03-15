package io.github.icodegarden.beecomb.executor.server;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.beecomb.executor.registry.JobHandlerRegistry;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.lang.metrics.MetricsOverload;
import io.github.icodegarden.commons.lang.result.Result2;
import io.github.icodegarden.commons.lang.result.Results;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JobReceiver {
	private static final Logger log = LoggerFactory.getLogger(JobReceiver.class);

	private volatile boolean closed;
	private AtomicLong processingCount = new AtomicLong(0);

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
		if (closed) {
			return Results.of(false, null, ExchangeFailedReason.serverRejected("Executor Closed", null));
		}

		JobHandler jobHandler = jobHandlerRegistry.getJobHandler(job.getJobHandlerName());
		if (jobHandler == null) {
			return Results.of(false, null, ExchangeFailedReason.serverRejected("No JobHandler", null));
		}
		boolean b = jobOverload.incrementOverload(job);
		if (!b) {
			return Results.of(false, null, ExchangeFailedReason.serverRejected("Exceed Overload", null));
		}

		processingCount.incrementAndGet();
		
		try {
			ExecuteJobResult result = jobHandler.handle(job);
			if (result == null) {
				/**
				 * build a result when user return null
				 */
				result = new ExecuteJobResult();
			}
			return Results.of(true, result, null);
		} catch (Exception e) {
			log.error("handle job failed, job:{}, jobHandler.name:{}", job, jobHandler.name(), e);
			String desc = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
			return Results.of(false, null, ExchangeFailedReason.serverException(desc, e));
		} finally {
			jobOverload.decrementOverload(job);

			if (processingCount.decrementAndGet() <= 0) {
				synchronized (this) {
					this.notify();
				}
			}
		}
	}

	/**
	 * 阻塞直到任务处理完毕或超时
	 * @param blockTimeoutMillis
	 */
	public void closeBlocking(long blockTimeoutMillis) {
		closed = true;
		if (processingCount.get() > 0) {
			synchronized (this) {
				try {
					this.wait(blockTimeoutMillis);
				} catch (InterruptedException ignore) {
				}
			}
		}
	}
}

package io.github.icodegarden.beecomb.worker.core;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.lang.result.Result3;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobEngine extends GracefullyShutdown {
	
	@Override
	String shutdownName();

	boolean allowEnQueue(ExecutableJobBO job);

	/**
	 * 不会抛出异常
	 * @param job
	 * @return enQueue的job，JobTrigger：挂载的对象，JobEngineException:如果有意外发生
	 */
	Result3<ExecutableJobBO,JobTrigger,JobEngineException> enQueue(ExecutableJobBO job);
	/**
	 * 
	 * @param enQueueResult 与enQueue结果保持一致
	 * @return
	 */
	boolean removeQueue(Result3<ExecutableJobBO,JobTrigger,JobEngineException> enQueueResult);
	/**
	 * 
	 * @return 已进队列的任务数
	 */
	int queuedSize();
	
	/**
	 * 关闭并阻塞直到处理完毕正在进行的任务或超时
	 * @param blockTimeoutMillis
	 */
	void shutdownBlocking(long blockTimeoutMillis);
	
	abstract class JobTrigger implements Runnable {
		private static final Logger log = LoggerFactory.getLogger(JobTrigger.class);
		
		private long executedTimes;
		private boolean running = false;
		private ScheduledFuture<?> future;

		@Override
		public void run() {
			running = true;
			try {
				doRun();
			} catch (Exception e) {
				// doRun 预期不会抛出异常，担保log
				log.error("ex on job run, expect no ex throw", e);
			} finally {
				running = false;
				executedTimes++;
			}
		}

		protected abstract void doRun();
		
		public long getExecutedTimes() {
			return executedTimes;
		}
		
		public boolean isRunning() {
			return running;
		}

		public ScheduledFuture<?> getFuture() {
			return future;
		}

		public void setFuture(ScheduledFuture<?> future) {
			this.future = future;
		}
	}
}

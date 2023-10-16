package io.github.icodegarden.beecomb.worker.core;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.nutrient.lang.lifecycle.GracefullyShutdown;
import io.github.icodegarden.nutrient.lang.result.Result3;

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
	 * 
	 * @param job
	 * @return enQueue的job，Object：enQueue结果，JobEngineException:如果有意外发生
	 */
	Result3<ExecutableJobBO, ? extends Object, JobEngineException> enQueue(ExecutableJobBO job);

	boolean removeQueue(ExecutableJobBO job);
	
	boolean run(ExecutableJobBO job);

	/**
	 * 
	 * @return 已进队列的任务数
	 */
	int queuedSize();

	/**
	 * 关闭并阻塞直到处理完毕正在进行的任务或超时
	 * 
	 * @param blockTimeoutMillis
	 */
	void shutdownBlocking(long blockTimeoutMillis);

}

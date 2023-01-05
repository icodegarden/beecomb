package io.github.icodegarden.beecomb.executor.registry;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobHandler {

	/**
	 * jobHandlerName
	 * 
	 * @return
	 */
	String name();

	/**
	 * 当执行成功时返回{@link ExecuteJobResult} <br>
	 * 若处理需要视为失败，则可以throw异常； schedule类型任务在下次时间继续调度，
	 * delay类型任务则需要根据所配置的retryOnExecuteFailed次数 和
	 * retryBackoffOnExecuteFailed决定是否继续触发 <br>
	 * 
	 * @return
	 */
	ExecuteJobResult handle(Job job) throws Exception;

	default void onParallelSuccess(Job job) throws Exception {
	}
}

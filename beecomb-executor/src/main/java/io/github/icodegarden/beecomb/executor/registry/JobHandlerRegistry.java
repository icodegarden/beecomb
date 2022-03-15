package io.github.icodegarden.beecomb.executor.registry;

import java.util.Collection;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobHandlerRegistry {
	/**
	 * 以追加的方式进行注册
	 * @param jobHandlers
	 */
	void registerAppend(Collection<? extends JobHandler> jobHandlers);
	/**
	 * 总是以覆盖的方式进行注册
	 * @param jobHandlers
	 */
	void registerReplace(Collection<? extends JobHandler> jobHandlers);
	/**
	 * 
	 * @param jobHandlerName
	 * @return Nullable
	 */
	JobHandler getJobHandler(String jobHandlerName);
	/**
	 * 获取所有已注册的
	 * @return
	 */
	Collection<? extends JobHandler> listJobHandlers();
	/**
	 * 注销指定的jobHandlers
	 * @param jobHandlers
	 */
	void deregister(Collection<? extends JobHandler> jobHandlers);
	/**
	 * 注销所有
	 */
	void deregisterAll();
}

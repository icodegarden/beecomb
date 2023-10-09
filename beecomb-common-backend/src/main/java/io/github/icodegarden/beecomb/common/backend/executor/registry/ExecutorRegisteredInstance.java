package io.github.icodegarden.beecomb.common.backend.executor.registry;

import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.commons.lang.metricsregistry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ExecutorRegisteredInstance extends RegisteredInstance {

	JobHandlerRegistrationBean getJobHandlerRegistrationBean();

}
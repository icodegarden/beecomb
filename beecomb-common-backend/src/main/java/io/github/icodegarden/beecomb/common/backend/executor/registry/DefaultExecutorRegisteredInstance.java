package io.github.icodegarden.beecomb.common.backend.executor.registry;

import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.nutrient.lang.metricsregistry.DefaultRegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DefaultExecutorRegisteredInstance extends DefaultRegisteredInstance implements ExecutorRegisteredInstance {

	private final JobHandlerRegistrationBean jobHandlerRegistrationBean;

	/**
	 * 
	 * @param serviceName
	 * @param instanceName
	 * @param ip
	 * @param port
	 * @param jobHandlerRegistrationBean Nullable
	 */
	public DefaultExecutorRegisteredInstance(String serviceName, String instanceName, String ip, int port,
			JobHandlerRegistrationBean jobHandlerRegistrationBean) {
		super(serviceName, instanceName, ip, port);
		this.jobHandlerRegistrationBean = jobHandlerRegistrationBean;
	}

	public JobHandlerRegistrationBean getJobHandlerRegistrationBean() {
		return jobHandlerRegistrationBean;
	}

	@Override
	public String toString() {
		return "[jobHandlerRegistrationBean=" + jobHandlerRegistrationBean + "," + super.toString() + "]";
	}

}
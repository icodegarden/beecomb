package io.github.icodegarden.beecomb.worker.registry;

import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ExecutorRegisteredInstance extends RegisteredInstance {

	JobHandlerRegistrationBean getJobHandlerRegistrationBean();

	public static class Default extends RegisteredInstance.Default implements ExecutorRegisteredInstance {

		private final JobHandlerRegistrationBean jobHandlerRegistrationBean;

		/**
		 * 
		 * @param serviceName
		 * @param instanceName
		 * @param ip
		 * @param port
		 * @param jobHandlerRegistrationBean Nullable
		 */
		public Default(String serviceName, String instanceName, String ip, int port,
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

}
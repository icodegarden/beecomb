package io.github.icodegarden.beecomb.worker.loadbalance;

import java.util.Queue;

import io.github.icodegarden.beecomb.common.backend.executor.registry.ExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.common.backend.executor.registry.ExecutorRegisteredInstance;
import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.exchange.loadbalance.MinimumLoadFirstInstanceLoadBalance;
import io.github.icodegarden.commons.lang.metricsregistry.FilterableInstanceDiscovery;
import io.github.icodegarden.commons.lang.metricsregistry.InstanceMetrics;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ExecutorInstanceLoadBalance implements InstanceLoadBalance {

	private final String serviceName = NodeRole.Executor.getRoleName();

	private final ExecutorInstanceDiscovery<ExecutorRegisteredInstance> executorInstanceDiscovery;
	private final InstanceMetrics instanceMetrics;
	private final String executorName;
	private final String jobHandlerName;

	public ExecutorInstanceLoadBalance(ExecutorInstanceDiscovery<ExecutorRegisteredInstance> executorInstanceDiscovery,
			InstanceMetrics instanceMetrics, String executorName, String jobHandlerName) {
		this.executorInstanceDiscovery = executorInstanceDiscovery;
		this.instanceMetrics = instanceMetrics;
		this.executorName = executorName;
		this.jobHandlerName = jobHandlerName;
	}

	/**
	 * serviceName 参数始终是executor
	 */
	@Override
	public Queue<MetricsInstance> selectCandidates(String ignore, int maxCandidate) {
		FilterableInstanceDiscovery<ExecutorRegisteredInstance> filterableInstanceDiscovery = new FilterableInstanceDiscovery<ExecutorRegisteredInstance>(
				instance -> {
					ExecutorRegisteredInstance executorRegisteredInstance = (ExecutorRegisteredInstance) instance;
					JobHandlerRegistrationBean jobHandlerRegistrationBean = executorRegisteredInstance
							.getJobHandlerRegistrationBean();
					if (jobHandlerRegistrationBean == null) {
						return false;
					}
					if (!executorName.equals(jobHandlerRegistrationBean.getExecutorName())) {
						return false;
					}
					return jobHandlerRegistrationBean.getJobHandlerRegistrations().stream()
							.anyMatch(jobHandlerRegistration -> {
								return jobHandlerName.equals(jobHandlerRegistration.getJobHandlerName());
							});
				}, executorInstanceDiscovery);

		MinimumLoadFirstInstanceLoadBalance instanceLoadBalance = new MinimumLoadFirstInstanceLoadBalance(
				filterableInstanceDiscovery, instanceMetrics);

		return instanceLoadBalance.selectCandidates(serviceName, maxCandidate);
	}

}

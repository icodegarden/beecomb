package io.github.icodegarden.beecomb.common.backend.executor.registry.zookeeper;

import io.github.icodegarden.beecomb.common.backend.executor.registry.DefaultExecutorRegisteredInstance;
import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.commons.zookeeper.metricsregistry.ZooKeeperRegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperExecutorRegisteredInstance extends DefaultExecutorRegisteredInstance
		implements ZooKeeperRegisteredInstance {

	private String znode;

	public ZooKeeperExecutorRegisteredInstance(String znode, String serviceName, String instanceName, String ip,
			int port, JobHandlerRegistrationBean jobHandlerRegistrationBean) {
		super(serviceName, instanceName, ip, port, jobHandlerRegistrationBean);
		this.znode = znode;
	}

	public String getZnode() {
		return znode;
	}

	@Override
	public String toString() {
		return "znode=" + znode + "," + super.toString();
	}

}
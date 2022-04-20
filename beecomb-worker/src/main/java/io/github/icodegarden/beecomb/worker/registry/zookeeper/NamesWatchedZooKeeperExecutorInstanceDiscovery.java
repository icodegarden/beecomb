package io.github.icodegarden.beecomb.worker.registry.zookeeper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.worker.registry.ExecutorInstanceDiscovery;
import io.github.icodegarden.commons.lang.registry.InstanceDiscovery;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.registry.NamesWatchedZooKeeperInstanceDiscovery;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperRegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NamesWatchedZooKeeperExecutorInstanceDiscovery
		implements ExecutorInstanceDiscovery<ZooKeeperExecutorRegisteredInstance> {

	private static final String SERVICE_NAME = NodeRole.Executor.getRoleName();

	private InstanceDiscovery<ZooKeeperRegisteredInstance> delegator;

	/**
	 * 
	 * @param zooKeeperHolder
	 * @param root            例如/beecomb
	 */
	public NamesWatchedZooKeeperExecutorInstanceDiscovery(ZooKeeperHolder zooKeeperHolder, String root,
			long cacheRefreshIntervalMillis) throws IllegalArgumentException {
		ZooKeeperExecutorInstanceDiscovery zooKeeperExecutorInstanceDiscovery = new ZooKeeperExecutorInstanceDiscovery(
				zooKeeperHolder, root);
		List<String> serviceNames = Arrays.asList(SERVICE_NAME);
		delegator = new NamesWatchedZooKeeperInstanceDiscovery(zooKeeperExecutorInstanceDiscovery, zooKeeperHolder,
				root, serviceNames, cacheRefreshIntervalMillis);
	}

	@Override
	public List<ZooKeeperExecutorRegisteredInstance> listNamedObjects(String ignore) {
		return listInstances();
	}

	private List<ZooKeeperExecutorRegisteredInstance> listInstances() {
		return (List) delegator.listInstances(SERVICE_NAME);
	}

	@Override
	public ZooKeeperExecutorRegisteredInstance parseInstance(Object data) {
		return (ZooKeeperExecutorRegisteredInstance) delegator.parseInstance(data);
	}

	@Override
	public void close() throws IOException {
		delegator.close();
	}
}

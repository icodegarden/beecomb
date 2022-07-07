package io.github.icodegarden.beecomb.master.discovery;

import java.util.List;

import org.apache.zookeeper.WatchedEvent;

import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.registry.NamesWatchedZooKeeperInstanceDiscovery;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceDiscovery;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperRegisteredInstance;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class ListenableNamesWatchedZooKeeperInstanceDiscovery extends NamesWatchedZooKeeperInstanceDiscovery {

	private List<InstanceDiscoveryListener> instanceDiscoveryListeners;

	public ListenableNamesWatchedZooKeeperInstanceDiscovery(
			ZooKeeperInstanceDiscovery<? extends ZooKeeperRegisteredInstance> delegator,
			ZooKeeperHolder zooKeeperHolder, String root, List<String> serviceNames, long cacheRefreshIntervalMillis)
			throws IllegalArgumentException {
		super(delegator, zooKeeperHolder, root, serviceNames, cacheRefreshIntervalMillis);
	}

	public void setInstanceDiscoveryListeners(List<InstanceDiscoveryListener> instanceDiscoveryListeners) {
		this.instanceDiscoveryListeners = instanceDiscoveryListeners;
	}

	@Override
	public void process(WatchedEvent event) {
		super.process(event);

		switch (event.getType()) {
		case NodeCreated: {
			String znode = event.getPath();
			ZooKeeperRegisteredInstance registeredInstance = parseInstance(znode);
			if (registeredInstance != null && instanceDiscoveryListeners != null) {
				for(InstanceDiscoveryListener instanceDiscoveryListener:instanceDiscoveryListeners) {
					instanceDiscoveryListener.onInstanceCreated(registeredInstance);	
				}
			}
		}
			break;
		case NodeDeleted: {
			String znode = event.getPath();
			ZooKeeperRegisteredInstance registeredInstance = parseInstance(znode);
			if (registeredInstance != null && instanceDiscoveryListeners != null) {
				for(InstanceDiscoveryListener instanceDiscoveryListener:instanceDiscoveryListeners) {
					instanceDiscoveryListener.onInstanceDeleted(registeredInstance);	
				}
			}
		}

			break;
		default:
			break;
		}
	}
}

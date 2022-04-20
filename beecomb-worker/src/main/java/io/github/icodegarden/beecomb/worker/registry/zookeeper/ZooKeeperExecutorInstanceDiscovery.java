package io.github.icodegarden.beecomb.worker.registry.zookeeper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.zookeeper.KeeperException;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.beecomb.worker.registry.ExecutorInstanceDiscovery;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.registry.ZnodePatternZooKeeperInstanceDiscovery;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceDiscovery;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperRegisteredInstance;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class ZooKeeperExecutorInstanceDiscovery
		implements ExecutorInstanceDiscovery<ZooKeeperExecutorRegisteredInstance>,
		ZooKeeperInstanceDiscovery<ZooKeeperExecutorRegisteredInstance> {

	private ZooKeeperInstanceDiscovery<ZooKeeperRegisteredInstance> zooKeeperInstanceDiscovery;
	private ZooKeeperHolder zooKeeperHolder;

	/**
	 * 
	 * @param zooKeeperHolder
	 * @param root            例如/beecomb
	 */
	public ZooKeeperExecutorInstanceDiscovery(ZooKeeperHolder zooKeeperHolder, String root)
			throws IllegalArgumentException {
		zooKeeperInstanceDiscovery = new ZnodePatternZooKeeperInstanceDiscovery(zooKeeperHolder, root);
		this.zooKeeperHolder = zooKeeperHolder;
	}

	@Override
	public List<ZooKeeperExecutorRegisteredInstance> listNamedObjects(String ignore) {
		return listInstances();
	}

	private List<ZooKeeperExecutorRegisteredInstance> listInstances() {
		final String serviceName = NodeRole.Executor.getRoleName();
		List<ZooKeeperRegisteredInstance> instances = zooKeeperInstanceDiscovery.listInstances(serviceName);

		List<ZooKeeperExecutorRegisteredInstance> collect = instances.stream().map(zooKeeperRegisteredInstance -> {
			return parseInstance(zooKeeperRegisteredInstance);
		}).filter(i -> i != null).collect(Collectors.toList());

		return collect;
	}

	@Override
	public ZooKeeperExecutorRegisteredInstance parseInstance(Object zooKeeperRegisteredInstanceObj) {
		if (zooKeeperRegisteredInstanceObj instanceof ZooKeeperRegisteredInstance) {
			ZooKeeperRegisteredInstance zooKeeperRegisteredInstance = (ZooKeeperRegisteredInstance) zooKeeperRegisteredInstanceObj;

			String znode = zooKeeperRegisteredInstance.getZnode();
			try {
				byte[] data = zooKeeperHolder.getConnectedZK().getData(znode, false, null);

				JobHandlerRegistrationBean jobHandlerRegistrationBean = null;
				if (data.length != 0) {
					jobHandlerRegistrationBean = JsonUtils.deserialize(new String(data, "utf-8"),
							JobHandlerRegistrationBean.class);
				}

				return new ZooKeeperExecutorRegisteredInstance(znode, zooKeeperRegisteredInstance.getServiceName(),
						zooKeeperRegisteredInstance.getInstanceName(), zooKeeperRegisteredInstance.getIp(),
						zooKeeperRegisteredInstance.getPort(), jobHandlerRegistrationBean);
			} catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
				log.error("WARNING ex on getData for listInstances znode:{}", znode, e);
				return null;
//				throw new ZooKeeperExceedExpectedException(
//						String.format("ex on getData for listInstances znode [%s]", znode), e);
			}
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		zooKeeperInstanceDiscovery.close();
	}
}

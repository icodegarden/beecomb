package io.github.icodegarden.beecomb.client;

import io.github.icodegarden.beecomb.client.ZooKeeperClientProperties.ZooKeeper;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ZooKeeperBeeCombClientTests extends AbstractBeeCombClientTests {

	@Override
	protected BeeCombClient getBeeCombClient() {
		ZooKeeper zookeeper = new ZooKeeperClientProperties.ZooKeeper(zkConnectString);
		ZooKeeperClientProperties properties = new ZooKeeperClientProperties(authentication, zookeeper);
		ZooKeeperBeeCombClient beeCombClient = new ZooKeeperBeeCombClient(properties);
		return beeCombClient;
	}

}

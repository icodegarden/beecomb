package io.github.icodegarden.beecomb.executor;

import io.github.icodegarden.beecomb.common.properties.ZooKeeper;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperSupportInstanceProperties extends InstanceProperties {

	private ZooKeeper zookeeper;

	public ZooKeeperSupportInstanceProperties(ZooKeeper zookeeper) {
		this.zookeeper = zookeeper;
	}

	public ZooKeeper getZookeeper() {
		return zookeeper;
	}

	public void setZookeeper(ZooKeeper zookeeper) {
		this.zookeeper = zookeeper;
	}

	@Override
	public String toString() {
		return "ZooKeeperSupportInstanceProperties [zookeeper=" + zookeeper + ", toString()=" + super.toString() + "]";
	}

}
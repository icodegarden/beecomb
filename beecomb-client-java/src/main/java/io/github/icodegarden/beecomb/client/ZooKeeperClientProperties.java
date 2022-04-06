package io.github.icodegarden.beecomb.client;

import io.github.icodegarden.beecomb.client.security.Authentication;
import io.github.icodegarden.beecomb.common.properties.ZooKeeper;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperClientProperties extends ClientProperties {

	private ZooKeeper zookeeper;
	
	private LoadBalance loadBalance = new LoadBalance();
	
	public ZooKeeperClientProperties(Authentication authentication, ZooKeeper zookeeper) {
		super(authentication);
		this.zookeeper = zookeeper;
	}

	public ZooKeeper getZookeeper() {
		return zookeeper;
	}

	public void setZookeeper(ZooKeeper zookeeper) {
		this.zookeeper = zookeeper;
	}

	public LoadBalance getLoadBalance() {
		return loadBalance;
	}

	public void setLoadBalance(LoadBalance loadBalance) {
		this.loadBalance = loadBalance;
	}

	@Override
	public String toString() {
		return "ZooKeeperSupportClientProperties [zookeeper=" + zookeeper + ", loadBalance=" + loadBalance
				+ ", toString()=" + super.toString() + "]";
	}

	public static class LoadBalance {
		private int maxCandidates = 3;

		public int getMaxCandidates() {
			return maxCandidates;
		}

		public void setMaxCandidates(int maxCandidates) {
			this.maxCandidates = maxCandidates;
		}

		@Override
		public String toString() {
			return "LoadBalance [maxCandidates=" + maxCandidates + "]";
		}
	}

	
}
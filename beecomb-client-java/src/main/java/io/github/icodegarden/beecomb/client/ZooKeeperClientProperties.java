package io.github.icodegarden.beecomb.client;

import java.util.Objects;

import io.github.icodegarden.beecomb.client.security.Authentication;

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

	//TODO 重复代码
	public static class ZooKeeper {
		private String root = "/beecomb";
		private String connectString;
		private int sessionTimeout = 3000;
		private int connectTimeout = 3000;
		private String aclAuth = "beecomb:beecomb";

		public ZooKeeper(String connectString) {
			Objects.requireNonNull(connectString, "connectString must not null");
			this.connectString = connectString;
		}

		public ZooKeeper(String root, String connectString, int sessionTimeout, int connectTimeout) {
			Objects.requireNonNull(root, "root must not null");
			Objects.requireNonNull(connectString, "connectString must not null");
			this.root = root;
			this.connectString = connectString;
			this.sessionTimeout = sessionTimeout;
			this.connectTimeout = connectTimeout;
		}

		public String getRoot() {
			return root;
		}

		public void setRoot(String root) {
			this.root = root;
		}

		public String getConnectString() {
			return connectString;
		}

		public void setConnectString(String connectString) {
			this.connectString = connectString;
		}

		public int getSessionTimeout() {
			return sessionTimeout;
		}

		public void setSessionTimeout(int sessionTimeout) {
			this.sessionTimeout = sessionTimeout;
		}

		public int getConnectTimeout() {
			return connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		public String getAclAuth() {
			return aclAuth;
		}

		public void setAclAuth(String aclAuth) {
			this.aclAuth = aclAuth;
		}

		@Override
		public String toString() {
			return "ZooKeeper [root=" + root + ", connectString=" + connectString + ", sessionTimeout=" + sessionTimeout
					+ ", connectTimeout=" + connectTimeout + ", aclAuth=" + aclAuth + "]";
		}
		
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
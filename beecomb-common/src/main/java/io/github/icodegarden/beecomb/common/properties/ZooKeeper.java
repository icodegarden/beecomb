package io.github.icodegarden.beecomb.common.properties;

import java.util.Objects;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeper {
	
	private String root = "/beecomb";
	private String connectString;
	private int sessionTimeout = 3000;
	private int connectTimeout = 3000;
	private String aclAuth = "beecomb:beecomb";

	public ZooKeeper() {
	}
	
	public ZooKeeper(String connectString) {
		Objects.requireNonNull(connectString, "connectString must not null");
		this.connectString = connectString;
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
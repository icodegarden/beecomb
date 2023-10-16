package io.github.icodegarden.beecomb.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.github.icodegarden.nutrient.zookeeper.ZooKeeperHolder;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ZookeeperBuilder4Test extends Properties4Test {

	protected ZooKeeperHolder zkh;

	@BeforeEach
	void initZK() throws Exception {
		ZooKeeperHolder.Config config = new ZooKeeperHolder.Config(zkConnectString, 30000, 10000);
		config.setAclAuth("beecomb:beecomb");
		zkh = new ZooKeeperHolder(config);
	}

	@AfterEach
	void closeZK() throws Exception {
		zkh.close();
	}

}

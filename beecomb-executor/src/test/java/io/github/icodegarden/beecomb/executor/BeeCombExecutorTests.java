package io.github.icodegarden.beecomb.executor;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import io.github.icodegarden.beecomb.test.Properties4Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class BeeCombExecutorTests extends Properties4Test {

	@Test
	void close() throws Exception {
		ZooKeeperSupportInstanceProperties instanceProperties = new ZooKeeperSupportInstanceProperties(
				new ZooKeeper(zkConnectString));
		BeeCombExecutor executor = BeeCombExecutor.start("test-executor", instanceProperties);

		executor.close();

//		assertThat(result2.getT2().getDesc()).isEqualTo("No JobHandler");
	}

}

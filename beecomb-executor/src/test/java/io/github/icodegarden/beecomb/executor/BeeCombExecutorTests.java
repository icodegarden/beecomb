package io.github.icodegarden.beecomb.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.beecomb.executor.registry.JobHandlerRegistry;
import io.github.icodegarden.beecomb.executor.server.JobReceiver;
import io.github.icodegarden.beecomb.test.Properties4Test;
import io.github.icodegarden.beecomb.test.ZookeeperBuilder4Test;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.lang.metrics.MetricsOverload;
import io.github.icodegarden.commons.lang.result.Result2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class BeeCombExecutorTests extends Properties4Test {

	@Test
	void close() throws Exception {
		ZooKeeperSupportInstanceProperties instanceProperties = new ZooKeeperSupportInstanceProperties(new ZooKeeperSupportInstanceProperties.ZooKeeper(zkConnectString));
		BeeCombExecutor executor = BeeCombExecutor.start("test-executor", instanceProperties);
		
		executor.close();

//		assertThat(result2.getT2().getDesc()).isEqualTo("No JobHandler");
	}

}

package io.github.icodegarden.beecomb.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.commons.exchange.InstanceExchangeResult;
import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.pool.NioClientSupplier;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NioClientSuppliers4Test {

	public static NioClientSupplier returnExchangeResultAlwaysSuccess() {
		return (ip, port) -> {
			// mock 成功的交互结果
			NioClient nioClient = mock(NioClient.class);
			InstanceExchangeResult exchangeResult = InstanceExchangeResult.clientWithoutExchange(true, null, null);
			doReturn(exchangeResult).when(nioClient).request(any(), anyInt());
			return nioClient;
		};
	}

	public static NioClientSupplier returnExchangeResultAlwaysSuccess(ExecuteJobResult executeJobResult) {
		return (ip, port) -> {
			// mock 成功的交互结果
			NioClient nioClient = mock(NioClient.class);
			InstanceExchangeResult exchangeResult = InstanceExchangeResult.clientWithoutExchange(true, executeJobResult,
					null, null);
			doReturn(exchangeResult).when(nioClient).request(any(), anyInt());
			return nioClient;
		};
	}

	public static NioClientSupplier returnExchangeResultAlwaysFailed() {
		return (ip, port) -> {
			// mock 失败的交互结果
			NioClient nioClient = mock(NioClient.class);
			InstanceExchangeResult exchangeResult = InstanceExchangeResult.clientWithoutExchange(false, null, null);
			doReturn(exchangeResult).when(nioClient).request(any(), anyInt());
			return nioClient;
		};
	}

	public static NioClientSupplier returnExchangeResultSuccessByList(List<Tuple2<String, Integer>> ipPortsSuccess) {
		return (ip, port) -> {
			if (ipPortsSuccess.contains(Tuples.of(ip, port))) {
				// mock 成功的交互结果
				NioClient nioClient = mock(NioClient.class);
				InstanceExchangeResult exchangeResult = InstanceExchangeResult.clientWithoutExchange(true, null, null);
				doReturn(exchangeResult).when(nioClient).request(any(), anyInt());
				return nioClient;
			}
			// mock 失败的交互结果
			NioClient nioClient = mock(NioClient.class);
			InstanceExchangeResult exchangeResult = InstanceExchangeResult.clientWithoutExchange(false, null, null);
			doReturn(exchangeResult).when(nioClient).request(any(), anyInt());
			return nioClient;
		};
	}
}

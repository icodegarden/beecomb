package io.github.icodegarden.beecomb.master.service;

import java.net.InetSocketAddress;
import java.util.Arrays;

import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.pojo.transfer.RequestWorkerDTO;
import io.github.icodegarden.beecomb.master.configuration.InstanceProperties;
import io.github.icodegarden.commons.exchange.CandidatesSwitchableExchanger;
import io.github.icodegarden.commons.exchange.ShardExchangeResult;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.DefaultMetricsInstance;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.exchange.nio.NioProtocol;
import io.github.icodegarden.commons.lang.registry.DefaultRegisteredInstance;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
import io.github.icodegarden.commons.nio.SerializerType;
import io.github.icodegarden.commons.nio.netty.NettyNioClient;
import io.github.icodegarden.commons.nio.pool.NioClientPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class InstanceRemoteService {

	private NioClientPool nioClientPool = NioClientPool.newPool(NodeRole.Master.getRoleName(), (ip, port) -> {
		NettyNioClient nioClient = new NettyNioClient(new InetSocketAddress(ip, port));
		nioClient.setSerializerType(SerializerType.Hessian2);
		return nioClient;
	});
	private NioProtocol protocol = new NioProtocol(nioClientPool);

	private static final RetryTemplate RETRY_TEMPLATE = RetryTemplate.builder().fixedBackoff(2000).maxAttempts(5)
			.retryOn(Exception.class).build();

	private final int exchangeTimeoutMillis;

	public InstanceRemoteService(InstanceProperties instanceProperties) {
		this.exchangeTimeoutMillis = instanceProperties.getJob().getDispatchTimeoutMillis();
	}

	public NioProtocol getProtocol() {
		return protocol;
	}

	/**
	 * 不会抛出异常<br>
	 * 会自动重试探测直到次数满
	 * 
	 * @return
	 */
	public boolean isLiveness(String ip, int port) {
		try {
			RETRY_TEMPLATE.execute(ctx -> {
				return ping(ip, port);
			});
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private String ping(String ip, int port) throws ExchangeException {
		RequestWorkerDTO dto = new RequestWorkerDTO(RequestWorkerDTO.METHOD_PING, null);
		String pong = (String) exchangeAssignedInstance(ip, port, dto);
		return pong;
	}

	Object exchangeAssignedInstance(String ip, int port, RequestWorkerDTO dto) throws ExchangeException {
		RegisteredInstance registered = new DefaultRegisteredInstance(NodeRole.Worker.getRoleName(), null, ip, port);
		MetricsInstance metricsInstance = new DefaultMetricsInstance(registered, null);
		CandidatesSwitchableExchanger exchanger = new CandidatesSwitchableExchanger(protocol,
				Arrays.asList(metricsInstance), false);

		ShardExchangeResult result = exchanger.exchange(dto, exchangeTimeoutMillis);
//			MetricsInstance loadBalancedInstance = result.successResult().instance();
		return result.response();
	}

}

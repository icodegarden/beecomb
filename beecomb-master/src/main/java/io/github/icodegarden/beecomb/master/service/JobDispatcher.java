package io.github.icodegarden.beecomb.master.service;

import java.net.InetSocketAddress;
import java.util.Collections;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.commons.exchange.CandidatesSwitchableLoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.LoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.ShardExchangeResult;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.exception.RequesterRejectedExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.exchange.nio.NioProtocol;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.Metrics.DimensionName;
import io.github.icodegarden.commons.nio.netty.NettyNioClient;
import io.github.icodegarden.commons.nio.pool.NioClientPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JobDispatcher {

	private final LoadBalanceExchanger<ShardExchangeResult> loadBalanceExchanger;
	private final int dispatchTimeoutMillis;

	public JobDispatcher(InstanceLoadBalance instanceLoadBalance, int dispatchTimeoutMillis, int maxCandidate) {
		NioClientPool nioClientPool = NioClientPool.newPool(NodeRole.Master.getRoleName(), (ip, port) -> {
			return new NettyNioClient(new InetSocketAddress(ip, port));
		});
		NioProtocol protocol = new NioProtocol(nioClientPool);

		CandidatesSwitchableLoadBalanceExchanger delegator = new CandidatesSwitchableLoadBalanceExchanger(protocol,
				instanceLoadBalance, NodeRole.Worker.getRoleName(), maxCandidate);
		loadBalanceExchanger = new MetricsManagedLoadBalanceExchanger(delegator);

		this.dispatchTimeoutMillis = dispatchTimeoutMillis;
	}

	/**
	 * 
	 * @param job
	 * @throws ExchangeException
	 * @return 成功的实例
	 */
	public MetricsInstance dispatch(ExecutableJobBO job) throws ExchangeException {
		ShardExchangeResult result = loadBalanceExchanger.exchange(job, dispatchTimeoutMillis);
		MetricsInstance loadBalancedInstance = result.successResult().instance();
		return loadBalancedInstance;
	}

	/**
	 * 发送成功则对对应实例的metrics增加 local 负载，以便对负载均衡起实时作用（因为Metrics使用了缓存）
	 * 
	 * @author Fangfang.Xu
	 */
	private class MetricsManagedLoadBalanceExchanger implements LoadBalanceExchanger<ShardExchangeResult> {

		private final CandidatesSwitchableLoadBalanceExchanger delegator;

		public MetricsManagedLoadBalanceExchanger(CandidatesSwitchableLoadBalanceExchanger delegator) {
			this.delegator = delegator;
		}

		@Override
		public ShardExchangeResult exchange(Object body, int timeout) throws ExchangeException {
			ShardExchangeResult result = delegator.exchange(body, timeout);

			ExecutableJobBO job = (ExecutableJobBO) body;

			MetricsInstance loadBalancedInstance = result.successResult().instance();
			Metrics metrics = loadBalancedInstance.getMetrics();
			metrics.incrementDimension(DimensionName.Jobs, job.ofOverload());

			return result;
		}

		@Override
		public ShardExchangeResult exchange(Object body, int timeout, InstanceLoadBalance instanceLoadBalance)
				throws ExchangeException {
			throw new RequesterRejectedExchangeException("Not Support", Collections.emptyList());
		}

	}

}

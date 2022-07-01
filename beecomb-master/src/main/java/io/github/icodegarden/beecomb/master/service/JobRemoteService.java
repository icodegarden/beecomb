package io.github.icodegarden.beecomb.master.service;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.transfer.RequestWorkerDTO;
import io.github.icodegarden.beecomb.common.pojo.view.RemoveJobVO;
import io.github.icodegarden.commons.exchange.CandidatesSwitchableExchanger;
import io.github.icodegarden.commons.exchange.CandidatesSwitchableLoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.LoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.ShardExchangeResult;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.exception.RequesterRejectedExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.DefaultMetricsInstance;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.exchange.nio.NioProtocol;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.Metrics.DimensionName;
import io.github.icodegarden.commons.lang.registry.DefaultRegisteredInstance;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
import io.github.icodegarden.commons.nio.netty.NettyNioClient;
import io.github.icodegarden.commons.nio.pool.NioClientPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JobRemoteService {

	private NioClientPool nioClientPool = NioClientPool.newPool(NodeRole.Master.getRoleName(), (ip, port) -> {
		return new NettyNioClient(new InetSocketAddress(ip, port));
	});
	private NioProtocol protocol = new NioProtocol(nioClientPool);

	private final LoadBalanceExchanger<ShardExchangeResult> loadBalanceExchanger;
	private final int dispatchTimeoutMillis;

	public JobRemoteService(InstanceLoadBalance instanceLoadBalance, int dispatchTimeoutMillis, int maxCandidate) {
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
	public MetricsInstance enQueue(ExecutableJobBO job) throws ExchangeException {
		RequestWorkerDTO dto = new RequestWorkerDTO(RequestWorkerDTO.METHOD_RECEIVEJOB, job);
		ShardExchangeResult result = loadBalanceExchanger.exchange(dto, dispatchTimeoutMillis);
		MetricsInstance loadBalancedInstance = result.successResult().instance();
		return loadBalancedInstance;
	}

	/**
	 * 如果在队列中，执行removeQueue；否则认为已被移除队列
	 * 
	 * @param job
	 * @return
	 * @throws ExchangeException
	 */
	public boolean removeQueue(ExecutableJobBO job) throws ExchangeException {
		if (job.getQueued()) {
			String ipport = job.getQueuedAtInstance();
			String[] split = ipport.split(":");

			RegisteredInstance registered = new DefaultRegisteredInstance(NodeRole.Worker.getRoleName(), null, split[0],
					Integer.parseInt(split[1]));
			MetricsInstance metricsInstance = new DefaultMetricsInstance(registered, null);
			CandidatesSwitchableExchanger exchanger = new CandidatesSwitchableExchanger(protocol,
					Arrays.asList(metricsInstance), false);

			RequestWorkerDTO dto = new RequestWorkerDTO(RequestWorkerDTO.METHOD_REMOVEJOB, job);
			ShardExchangeResult result = exchanger.exchange(dto, dispatchTimeoutMillis);
			RemoveJobVO vo = (RemoveJobVO) result.response();
//			MetricsInstance loadBalancedInstance = result.successResult().instance();
			return vo.getRemoved();
		}

		return true;
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

			RequestWorkerDTO dto = (RequestWorkerDTO) body;

			if (dto.getMethod() == RequestWorkerDTO.METHOD_RECEIVEJOB) {
				/**
				 * 增加
				 */
				ExecutableJobBO job = (ExecutableJobBO) dto.getBody();

				MetricsInstance loadBalancedInstance = result.successResult().instance();
				Metrics metrics = loadBalancedInstance.getMetrics();
				metrics.incrementDimension(DimensionName.Jobs, job.ofOverload());
			}
			/**
			 * 减少不需要很强的实时性
			 */

			return result;
		}

		@Override
		public ShardExchangeResult exchange(Object body, int timeout, InstanceLoadBalance instanceLoadBalance)
				throws ExchangeException {
			throw new RequesterRejectedExchangeException("Not Support", Collections.emptyList());
		}

	}

}

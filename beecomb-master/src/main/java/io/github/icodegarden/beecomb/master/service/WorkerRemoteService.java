package io.github.icodegarden.beecomb.master.service;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.transfer.RequestWorkerDTO;
import io.github.icodegarden.beecomb.common.pojo.view.RemoveQueueVO;
import io.github.icodegarden.beecomb.master.configuration.InstanceProperties;
import io.github.icodegarden.commons.exchange.CandidatesSwitchableLoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.LoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.ShardExchangeResult;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.exception.RequesterRejectedExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.Metrics.DimensionName;
import io.github.icodegarden.commons.lang.registry.InstanceDiscovery;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperRegisteredInstance;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Service
public class WorkerRemoteService {

	@Autowired
	private InstanceDiscovery<ZooKeeperRegisteredInstance> zooKeeperInstanceDiscovery;
	@Autowired
	private InstanceRemoteService instanceRemoteService;
	@Autowired
	private InstanceProperties instanceProperties;
	@Autowired
	private InstanceLoadBalance instanceLoadBalance;

	private LoadBalanceExchanger<ShardExchangeResult> loadBalanceExchanger;
	private int exchangeTimeoutMillis;

	@PostConstruct
	void init() {
		this.exchangeTimeoutMillis = instanceProperties.getJob().getDispatchTimeoutMillis();

		CandidatesSwitchableLoadBalanceExchanger delegator = new CandidatesSwitchableLoadBalanceExchanger(
				instanceRemoteService.getProtocol(), instanceLoadBalance, NodeRole.Worker.getRoleName(),
				instanceProperties.getLoadBalance().getMaxCandidates());
		loadBalanceExchanger = new MetricsManagedLoadBalanceExchanger(delegator);
	}

	/**
	 * 
	 * @param job
	 * @throws ExchangeException
	 * @return ???????????????
	 */
	public MetricsInstance enQueue(ExecutableJobBO job) throws ExchangeException {
		RequestWorkerDTO dto = new RequestWorkerDTO(RequestWorkerDTO.METHOD_RECEIVEJOB, job);
		ShardExchangeResult result = loadBalanceExchanger.exchange(dto, exchangeTimeoutMillis);
		MetricsInstance loadBalancedInstance = result.successResult().instance();
		return loadBalancedInstance;
	}

	/**
	 * ???????????????????????????removeQueue?????????????????????????????????
	 * 
	 * @param job
	 * @return
	 * @throws ExchangeException
	 */
	public boolean removeQueue(ExecutableJobBO job) throws ExchangeException {
		if (log.isInfoEnabled()) {
			log.info("removeQueue job.id:{} that queued:{}", job.getId(), job.getQueued());
		}
		if (job.getQueued()) {
			String ipport = job.getQueuedAtInstance();
			String[] split = ipport.split(":");

			String ip = split[0];
			int port = Integer.parseInt(split[1]);

			RequestWorkerDTO dto = new RequestWorkerDTO(RequestWorkerDTO.METHOD_REMOVEJOB, job);
			try {
				RemoveQueueVO vo = (RemoveQueueVO) instanceRemoteService.exchangeAssignedInstance(ip, port, dto);
				return vo.getRemoved();
			} catch (ExchangeException e) {
				/**
				 * ???Worker???????????????????????????????????????????????????
				 */
				boolean online = isOnline(ipport);
				if (online) {
					throw e;
				}
				boolean liveness = instanceRemoteService.isLiveness(ip, port);
				if (liveness) {
					throw e;
				}
				return true;
			}
		}

		return true;
	}

	public int queuedSize(String ip, int port) throws ExchangeException {
		RequestWorkerDTO dto = new RequestWorkerDTO(RequestWorkerDTO.METHOD_QUEUEDSIZE, null);
		int count = (int) instanceRemoteService.exchangeAssignedInstance(ip, port, dto);
		return count;
	}

	private boolean isOnline(String ipport) {
		/**
		 * ???NamesWatchedZooKeeperInstanceDiscovery????????????
		 */
		List<ZooKeeperRegisteredInstance> instances = zooKeeperInstanceDiscovery
				.listInstances(NodeRole.Worker.getRoleName());
		boolean match = instances.stream().anyMatch(instance -> {
			return SystemUtils.formatIpPort(instance.getIp(), instance.getPort()).equals(ipport);
		});

		return match;
	}

	/**
	 * ?????????????????????????????????metrics?????? local ??????????????????????????????????????????????????????Metrics??????????????????
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
				 * ??????
				 */
				ExecutableJobBO job = (ExecutableJobBO) dto.getBody();

				MetricsInstance loadBalancedInstance = result.successResult().instance();
				Metrics metrics = loadBalancedInstance.getMetrics();
				metrics.incrementDimension(DimensionName.Jobs, job.ofOverload());
			}
			/**
			 * ?????????????????????????????????
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

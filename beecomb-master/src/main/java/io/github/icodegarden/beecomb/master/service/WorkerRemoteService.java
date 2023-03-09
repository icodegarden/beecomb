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
import io.github.icodegarden.beecomb.common.pojo.view.RunJobVO;
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
import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;
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
	 * @return 成功的实例
	 */
	public MetricsInstance enQueue(ExecutableJobBO job) throws ExchangeException {
		RequestWorkerDTO dto = new RequestWorkerDTO(RequestWorkerDTO.METHOD_RECEIVEJOB, job);
		ShardExchangeResult result = loadBalanceExchanger.exchange(dto, exchangeTimeoutMillis);
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
		if (log.isInfoEnabled()) {
			log.info("removeQueue job.id:{} that queued:{}", job.getId(), job.getQueued());
		}
		if (job.getQueued()) {
			String ipport = job.getQueuedAtInstance();
			Tuple2<String, Integer> tuple2 = resolveIpPort(ipport);

			String ip = tuple2.getT1();
			int port = tuple2.getT2();

			RequestWorkerDTO dto = new RequestWorkerDTO(RequestWorkerDTO.METHOD_REMOVEJOB, job);
			try {
				RemoveQueueVO vo = (RemoveQueueVO) instanceRemoteService.exchangeAssignedInstance(ip, port, dto);
				return vo.getRemoved();
			} catch (ExchangeException e) {
				/**
				 * 若Worker不在线且不健康，任务实际已不在队列
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
	
	public boolean runJob(ExecutableJobBO job) throws ExchangeException {
		if (log.isInfoEnabled()) {
			log.info("run job.id:{} that queued:{}", job.getId(), job.getQueued());
		}
		if (job.getQueued()) {
			String ipport = job.getQueuedAtInstance();
			Tuple2<String, Integer> tuple2 = resolveIpPort(ipport);

			String ip = tuple2.getT1();
			int port = tuple2.getT2();

			RequestWorkerDTO dto = new RequestWorkerDTO(RequestWorkerDTO.METHOD_RUNJOB, job);
			RunJobVO vo = (RunJobVO) instanceRemoteService.exchangeAssignedInstance(ip, port, dto);
			return vo.getSuccess();
		}

		return false;
	}

	public int queuedSize(String ip, int port) throws ExchangeException {
		RequestWorkerDTO dto = new RequestWorkerDTO(RequestWorkerDTO.METHOD_QUEUEDSIZE, null);
		int count = (int) instanceRemoteService.exchangeAssignedInstance(ip, port, dto);
		return count;
	}

	private boolean isOnline(String ipport) {
		/**
		 * 从NamesWatchedZooKeeperInstanceDiscovery中获取的
		 */
		List<ZooKeeperRegisteredInstance> instances = zooKeeperInstanceDiscovery
				.listInstances(NodeRole.Worker.getRoleName());
		boolean match = instances.stream().anyMatch(instance -> {
			return SystemUtils.formatIpPort(instance.getIp(), instance.getPort()).equals(ipport);
		});

		return match;
	}
	
	private Tuple2<String, Integer> resolveIpPort(String ipport) {
		String[] split = ipport.split(":");

		String ip = split[0];
		int port = Integer.parseInt(split[1]);
		return Tuples.of(ip, port);
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

package io.github.icodegarden.beecomb.client;

import java.io.IOException;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import io.github.icodegarden.nutrient.exchange.CandidatesSwitchableLoadBalanceExchanger;
import io.github.icodegarden.nutrient.exchange.Exchanger;
import io.github.icodegarden.nutrient.exchange.Protocol;
import io.github.icodegarden.nutrient.exchange.ShardExchangeResult;
import io.github.icodegarden.nutrient.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.nutrient.exchange.loadbalance.RoundRobinInstanceLoadBalance;
import io.github.icodegarden.nutrient.lang.metricsregistry.InstanceDiscovery;
import io.github.icodegarden.nutrient.lang.metricsregistry.RegisteredInstance;
import io.github.icodegarden.nutrient.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.nutrient.zookeeper.metricsregistry.ZnodePatternZooKeeperInstanceDiscovery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperBeeCombClient extends AbstractBeeCombClient {

	private final ZooKeeperClientProperties clientProperties;
	private final ZooKeeperHolder zooKeeperHolder;
	private final InstanceDiscovery<? extends RegisteredInstance> instanceDiscovery;
	private final InstanceLoadBalance instanceLoadBalance;

	public ZooKeeperBeeCombClient(ZooKeeperClientProperties clientProperties) {
		super(clientProperties);
		this.clientProperties = clientProperties;

		ZooKeeper zookeeper = clientProperties.getZookeeper();
		ZooKeeperHolder.Config config = new ZooKeeperHolder.Config(zookeeper.getConnectString(), zookeeper.getSessionTimeout(),
				zookeeper.getConnectTimeout());
		config.setAclAuth(zookeeper.getAclAuth());
		
		this.zooKeeperHolder = new ZooKeeperHolder(config);
		this.instanceDiscovery = new ZnodePatternZooKeeperInstanceDiscovery(zooKeeperHolder, zookeeper.getRoot());
		this.instanceLoadBalance = new RoundRobinInstanceLoadBalance(instanceDiscovery);
	}

	@Override
	protected String pathPrefix() {
		return "";
	}

	@Override
	protected Exchanger<ShardExchangeResult> buildExchanger(Protocol protocol) {
		return new CandidatesSwitchableLoadBalanceExchanger(protocol, instanceLoadBalance,
				NodeRole.Master.getRoleName(), clientProperties.getLoadBalance().getMaxCandidates());
	}

	@Override
	public void close() throws IOException {
		instanceDiscovery.close();
		zooKeeperHolder.close();
	}
}

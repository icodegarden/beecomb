package io.github.icodegarden.beecomb.client;

import java.io.IOException;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import io.github.icodegarden.commons.exchange.CandidatesSwitchableLoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.Exchanger;
import io.github.icodegarden.commons.exchange.Protocol;
import io.github.icodegarden.commons.exchange.ShardExchangeResult;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.RoundRobinInstanceLoadBalance;
import io.github.icodegarden.commons.lang.registry.InstanceDiscovery;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder.Config;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceDiscovery;

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
		Config config = new ZooKeeperHolder.Config(zookeeper.getConnectString(), zookeeper.getSessionTimeout(),
				zookeeper.getConnectTimeout());
		config.setAclAuth(zookeeper.getAclAuth());
		
		this.zooKeeperHolder = new ZooKeeperHolder(config);
		this.instanceDiscovery = new ZooKeeperInstanceDiscovery.Default(zooKeeperHolder, zookeeper.getRoot());
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

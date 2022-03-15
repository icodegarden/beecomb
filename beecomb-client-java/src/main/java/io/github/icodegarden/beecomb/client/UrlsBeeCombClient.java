package io.github.icodegarden.beecomb.client;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import io.github.icodegarden.beecomb.common.enums.NodeRole;
import io.github.icodegarden.commons.exchange.CandidatesSwitchableExchanger;
import io.github.icodegarden.commons.exchange.Exchanger;
import io.github.icodegarden.commons.exchange.Protocol;
import io.github.icodegarden.commons.exchange.ShardExchangeResult;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class UrlsBeeCombClient extends AbstractBeeCombClient {

	private String pathPrefix = "";
	private List<MetricsInstance> candidates;

	/**
	 * 
	 * @param urls             {scheme}://{ip}:{port}/{path}
	 * @param clientProperties
	 */
	public UrlsBeeCombClient(UrlsClientProperties clientProperties) {
		super(clientProperties);

		candidates = clientProperties.getUrls().stream().map(address -> {
			try {
				URL url = new URL(address);
				String protocol = url.getProtocol();
				String host = url.getHost();
				int port = url.getPort();
				String path = url.getPath();

				RegisteredInstance registeredInstance = new RegisteredInstance.Default(NodeRole.Master.getRoleName(),
						"unknown", protocol, host, port);
				MetricsInstance metricsInstance = new MetricsInstance.Default(registeredInstance, null);

				pathPrefix = path != null ? path : pathPrefix;
				if (pathPrefix.endsWith("/")) {
					pathPrefix.substring(0, pathPrefix.length() - 1);
				}

				return metricsInstance;
			} catch (Exception e) {
				throw new IllegalArgumentException("ex on init client", e);
			}
		}).collect(Collectors.toList());
	}
	
	@Override
	protected String pathPrefix() {
		return pathPrefix;
	}
	
	@Override
	protected Exchanger<ShardExchangeResult> buildExchanger(Protocol protocol) {
		return new CandidatesSwitchableExchanger(protocol, candidates, false);
	}

	@Override
	public void close() throws IOException {
	}
}

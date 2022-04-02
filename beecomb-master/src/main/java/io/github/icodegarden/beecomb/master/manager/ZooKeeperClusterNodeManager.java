package io.github.icodegarden.beecomb.master.manager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.master.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.master.pojo.query.ClusterNodeQuery;
import io.github.icodegarden.beecomb.master.pojo.view.ClusterNodeVO;
import io.github.icodegarden.beecomb.master.pojo.view.ClusterNodeVO.MetricsDimension;
import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.registry.InstanceDiscovery;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
import io.github.icodegarden.commons.lang.util.CollectionUtils;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.metrics.ZooKeeperInstanceMetrics;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceDiscovery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class ZooKeeperClusterNodeManager implements ClusterNodeManager {

	private InstanceDiscovery<? extends RegisteredInstance> instanceDiscovery;
	private InstanceMetrics<? extends Metrics> instanceMetrics;

	public ZooKeeperClusterNodeManager(ZooKeeperHolder zooKeeperHolder, InstanceProperties instanceProperties) {
		/**
		 * 实例需要构造而不是注入的原因是：可注入的实例是缓存式的，不用于Master、Executor
		 */
		instanceDiscovery = new ZooKeeperInstanceDiscovery.Default(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot());

		instanceMetrics = new ZooKeeperInstanceMetrics.Default(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot());
	}

	@Override
	public Page<ClusterNodeVO> pageNodes(ClusterNodeQuery query) {
		List<? extends RegisteredInstance> allInstances = instanceDiscovery.listInstances(query.getServiceName());
		if (allInstances.isEmpty()) {
			return new Page<ClusterNodeVO>(query.getPage(), query.getSize());
		}
		if (StringUtils.hasText(query.getIp())) {
			allInstances = allInstances.stream().filter(instance -> instance.getIp().contains(query.getIp()))
					.collect(Collectors.toList());
		}

		// 总数量
		final int total = allInstances.size();
		// 总页数
		final int pages = total % query.getSize() > 0 ? (total / query.getSize()) + 1 : total / query.getSize();

		List<? extends RegisteredInstance> pageInstances = CollectionUtils.subSafely(allInstances,
				(query.getPage() - 1) * query.getSize(), query.getSize());

		List<? extends Metrics> metrics = instanceMetrics.listMetrics(query.getServiceName());
		Map<String, ? extends Metrics> metricsMap = metrics.stream()
				.collect(Collectors.toMap(Metrics::getInstanceName, v -> v));

		List<ClusterNodeVO> resultList = pageInstances.stream().map(instance -> {
			Metrics m = metricsMap.get(instance.getInstanceName());

			List<MetricsDimension> metricsDimensions = null;
			if (m != null) {
				metricsDimensions = m.getDimensions().values().stream().map(d -> {
					return new ClusterNodeVO.MetricsDimension(d.getDimensionName().getValue(), d.getMax(), d.getUsed(),
							d.getWeight(), d.getDesc());
				}).collect(Collectors.toList());
			}

			return new ClusterNodeVO(instance.getServiceName(), instance.getInstanceName(), instance.getIp(),
					instance.getPort(), metricsDimensions);
		}).collect(Collectors.toList());

		Page<ClusterNodeVO> p = new Page<ClusterNodeVO>(query.getPage(), query.getSize());
		p.setPages(pages);
		p.setTotal(total);
		p.addAll(resultList);

		return p;
	}
}

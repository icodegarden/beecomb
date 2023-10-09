package io.github.icodegarden.beecomb.master.manager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.backend.executor.registry.ExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.common.backend.executor.registry.ExecutorRegisteredInstance;
import io.github.icodegarden.beecomb.common.backend.executor.registry.zookeeper.ZooKeeperExecutorInstanceDiscovery;
import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.beecomb.master.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.master.pojo.query.ClusterNodeQuery;
import io.github.icodegarden.beecomb.master.pojo.view.ClusterNodeVO;
import io.github.icodegarden.beecomb.master.pojo.view.ClusterNodeVO.MetricsDimension;
import io.github.icodegarden.commons.lang.metricsregistry.InstanceDiscovery;
import io.github.icodegarden.commons.lang.metricsregistry.InstanceMetrics;
import io.github.icodegarden.commons.lang.metricsregistry.Metrics;
import io.github.icodegarden.commons.lang.metricsregistry.RegisteredInstance;
import io.github.icodegarden.commons.lang.util.CollectionUtils;
import io.github.icodegarden.commons.springboot.SpringContext;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.metricsregistry.ZnodeDataZooKeeperInstanceMetrics;
import io.github.icodegarden.commons.zookeeper.metricsregistry.ZnodePatternZooKeeperInstanceDiscovery;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class ZooKeeperClusterNodeManager implements ClusterNodeManager {

	private InstanceDiscovery<? extends RegisteredInstance> instanceDiscovery;
	ExecutorInstanceDiscovery<? extends ExecutorRegisteredInstance> executorInstanceDiscovery;
	private InstanceMetrics<? extends Metrics> instanceMetrics;

	public ZooKeeperClusterNodeManager(ZooKeeperHolder zooKeeperHolder, InstanceProperties instanceProperties) {
		/**
		 * 实例需要构造而不是注入的原因是：可注入的实例是缓存式的，不用于Master、Executor
		 */
		instanceDiscovery = new ZnodePatternZooKeeperInstanceDiscovery(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot());

		executorInstanceDiscovery = new ZooKeeperExecutorInstanceDiscovery(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot());

		instanceMetrics = new ZnodeDataZooKeeperInstanceMetrics(zooKeeperHolder,
				instanceProperties.getZookeeper().getRoot());
	}

	@Override
	public Page<ClusterNodeVO> pageNodes(ClusterNodeQuery query) {
		NodePager nodePager = SpringContext.getApplicationContext().getBean(query.getServiceName(), NodePager.class);
		return nodePager.pageNodes(query);
	}

	private abstract class NodePager {

		protected abstract InstanceDiscovery<? extends RegisteredInstance> getInstanceDiscovery();

		public Page<ClusterNodeVO> pageNodes(ClusterNodeQuery query) {
			List<? extends RegisteredInstance> allInstances = getInstanceDiscovery()
					.listInstances(query.getServiceName());
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

			List<ClusterNodeVO> resultList = convertInstances(pageInstances, metricsMap);

			Page<ClusterNodeVO> p = new Page<ClusterNodeVO>(query.getPage(), query.getSize());
			p.setPages(pages);
			p.setTotal(total);
			p.addAll(resultList);

			return p;
		}

		protected abstract List<ClusterNodeVO> convertInstances(List<? extends RegisteredInstance> pageInstances,
				Map<String, ? extends Metrics> metricsMap);
	}

	private class MWNodePager extends NodePager {
		@Override
		protected InstanceDiscovery<? extends RegisteredInstance> getInstanceDiscovery() {
			return instanceDiscovery;
		}

		@Override
		protected List<ClusterNodeVO> convertInstances(List<? extends RegisteredInstance> pageInstances,
				Map<String, ? extends Metrics> metricsMap) {
			List<ClusterNodeVO> resultList = pageInstances.stream().map(instance -> {
				Metrics m = metricsMap.get(instance.getInstanceName());

				List<MetricsDimension> metricsDimensions = null;
				if (m != null) {
					metricsDimensions = m.getDimensions().values().stream().map(d -> {
						return new ClusterNodeVO.MetricsDimension(d.getDimensionName().getValue(),
								new BigDecimal(d.getMax()).setScale(4, RoundingMode.HALF_UP).doubleValue(),
								new BigDecimal(d.getUsed()).setScale(4, RoundingMode.HALF_UP).doubleValue(),
								d.getWeight(), d.getDesc());
					}).collect(Collectors.toList());
				}

				return new ClusterNodeVO(instance.getServiceName(), instance.getInstanceName(), instance.getIp(),
						instance.getPort(), metricsDimensions);
			}).collect(Collectors.toList());

			return resultList;
		}
	}

	@Service("master")
	private class MasterPager extends MWNodePager {
	}

	@Service("worker")
	private class WorkerNodePager extends MWNodePager {
	}

	@Service("executor")
	private class ExecutorNodePager extends NodePager {
		@Override
		protected InstanceDiscovery<? extends RegisteredInstance> getInstanceDiscovery() {
			return executorInstanceDiscovery;
		}

		@Override
		protected List<ClusterNodeVO> convertInstances(List<? extends RegisteredInstance> pageInstances,
				Map<String, ? extends Metrics> metricsMap) {
			List<ClusterNodeVO> resultList = pageInstances.stream().map(item -> {
				ExecutorRegisteredInstance instance = (ExecutorRegisteredInstance) item;
				JobHandlerRegistrationBean jobHandlerRegistrationBean = instance.getJobHandlerRegistrationBean();

				Metrics m = metricsMap.get(instance.getInstanceName());

				List<MetricsDimension> metricsDimensions = null;
				if (m != null) {
					metricsDimensions = m.getDimensions().values().stream().map(d -> {
						return new ClusterNodeVO.MetricsDimension(d.getDimensionName().getValue(),
								new BigDecimal(d.getMax()).setScale(4, RoundingMode.HALF_UP).doubleValue(),
								new BigDecimal(d.getUsed()).setScale(4, RoundingMode.HALF_UP).doubleValue(),
								d.getWeight(), d.getDesc());
					}).collect(Collectors.toList());
				}

				return new ExecutorClusterNodeVO(instance.getServiceName(), instance.getInstanceName(),
						instance.getIp(), instance.getPort(), metricsDimensions, jobHandlerRegistrationBean);
			}).collect(Collectors.toList());

			return resultList;
		}
	}

	@Getter
	@Setter
	@ToString
	public static class ExecutorClusterNodeVO extends ClusterNodeVO {
		private final JobHandlerRegistrationBean jobHandlerRegistrationBean;

		public ExecutorClusterNodeVO(String serviceName, String instanceName, String ip, int port,
				List<MetricsDimension> metricsDimensions, JobHandlerRegistrationBean jobHandlerRegistrationBean) {
			super(serviceName, instanceName, ip, port, metricsDimensions);
			this.jobHandlerRegistrationBean = jobHandlerRegistrationBean;
		}
	}
}

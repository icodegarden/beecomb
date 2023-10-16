package io.github.icodegarden.beecomb.executor.registry.zookeeper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean.JobHandlerRegistration;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.beecomb.executor.registry.JobHandlerRegistry;
import io.github.icodegarden.nutrient.lang.annotation.Nullable;
import io.github.icodegarden.nutrient.lang.util.JsonUtils;
import io.github.icodegarden.nutrient.zookeeper.NewZooKeeperListener;
import io.github.icodegarden.nutrient.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.nutrient.zookeeper.exception.ZooKeeperException;
import io.github.icodegarden.nutrient.zookeeper.metricsregistry.ZooKeeperInstanceRegistry;
import io.github.icodegarden.nutrient.zookeeper.metricsregistry.ZooKeeperRegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperJobHandlerRegistry implements JobHandlerRegistry, NewZooKeeperListener {
	private static final Logger log = LoggerFactory.getLogger(ZooKeeperJobHandlerRegistry.class);

	private Map<String/* name */, JobHandler> name_jobHandlers;

	private final String namespace;
	private final String executorName;
	private final ZooKeeperInstanceRegistry zooKeeperInstanceRegistry;

	public ZooKeeperJobHandlerRegistry(@Nullable String namespace, String executorName, ZooKeeperHolder zooKeeperHolder,
			ZooKeeperInstanceRegistry zooKeeperInstanceRegistry) {
		this.namespace = namespace;
		this.executorName = executorName;
		this.zooKeeperInstanceRegistry = zooKeeperInstanceRegistry;

		zooKeeperHolder.addNewZooKeeperListener(this);
	}

	@Override
	public void registerAppend(Collection<? extends JobHandler> jobHandlers) {
		if (jobHandlers == null || jobHandlers.isEmpty()) {
			throw new IllegalArgumentException("jobHandlers must not empty");
		}

		List<JobHandler> all = new ArrayList<JobHandler>(jobHandlers);
		if (name_jobHandlers != null && !name_jobHandlers.isEmpty()) {
			all.addAll(name_jobHandlers.values());
		}

		registerReplaceValidateDuplicate(all);
	}

	@Override
	public void registerReplace(Collection<? extends JobHandler> jobHandlers) throws ZooKeeperException {
		if (jobHandlers == null || jobHandlers.isEmpty()) {
			throw new IllegalArgumentException("jobHandlers must not empty");
		}

		registerReplaceValidateDuplicate(jobHandlers);
	}

	/**
	 * 重复校验并以覆盖式注册
	 * 
	 * @param jobHandlers
	 * @throws ZooKeeperException
	 */
	private void registerReplaceValidateDuplicate(Collection<? extends JobHandler> jobHandlers)
			throws ZooKeeperException {
		Set<JobHandler> set = new HashSet<JobHandler>();
		for (JobHandler jobHandler : jobHandlers) {
			for (JobHandler added : set) {
				if (Objects.equals(added.name(), jobHandler.name())) {
					throw new IllegalArgumentException(
							String.format("duplicate JobHandler name [%s]", jobHandler.name()));
				}
			}
			set.add(jobHandler);
		}
		doRegister(set);
	}

	private void registerEmpty() throws ZooKeeperException {
		doRegister(Collections.emptyList());
	}

	/**
	 * 覆盖式的
	 * 
	 * @param jobHandlers
	 * @throws ZooKeeperException
	 */
	private void doRegister(Collection<? extends JobHandler> jobHandlers) throws ZooKeeperException {
		if (jobHandlers == null) {
			throw new IllegalArgumentException("jobHandlers must not null");
		}
		if (log.isInfoEnabled()) {
			log.info("register jobHandlers:{}", jobHandlers);
		}

		ZooKeeperRegisteredInstance instance = zooKeeperInstanceRegistry.registerIfNot();
		final String nodeName = instance.getZnode();

		JobHandlerRegistrationBean jobHandlerRegistrationBean = new JobHandlerRegistrationBean();
		jobHandlerRegistrationBean.setNamespace(namespace);
		jobHandlerRegistrationBean.setExecutorName(executorName);
		Set<JobHandlerRegistration> jobHandlerRegistrations = jobHandlers.stream().map(jobHandler -> {
			JobHandlerRegistration jobHandlerRegistration = new JobHandlerRegistrationBean.JobHandlerRegistration();
			jobHandlerRegistration.setJobHandlerName(jobHandler.name());
			return jobHandlerRegistration;
		}).collect(Collectors.toSet());
		jobHandlerRegistrationBean.setJobHandlerRegistrations(jobHandlerRegistrations);

		String json = JsonUtils.serialize(jobHandlerRegistrationBean);
		byte[] data = json.getBytes(StandardCharsets.UTF_8);
		zooKeeperInstanceRegistry.setData(data);

		name_jobHandlers = jobHandlers.stream().collect(Collectors.toMap(JobHandler::name, i -> i));
	}

	@Override
	public JobHandler getJobHandler(String jobHandlerName) {
		return name_jobHandlers.get(jobHandlerName);
	}

	@Override
	public Collection<? extends JobHandler> listJobHandlers() {
		return name_jobHandlers.values();
	}

	@Override
	public void deregister(Collection<? extends JobHandler> jobHandlers) {
		List<JobHandler> filterd = name_jobHandlers.values().stream().filter(h -> {
			return !jobHandlers.contains(h);
		}).collect(Collectors.toList());

		registerReplace(filterd);
	}

	@Override
	public void deregisterAll() {
		registerEmpty();
	}

	@Override
	public void onNewZooKeeper() {
		/**
		 * 保障zk重新建立session后能够自动刷入JobHandlers
		 */
		if (log.isInfoEnabled()) {
			log.info("do re register jobHandlers after new ZooKeeper");
		}
		registerReplace(listJobHandlers());
	}
}

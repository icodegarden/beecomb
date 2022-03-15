package io.github.icodegarden.beecomb.executor.registry.zookeeper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean;
import io.github.icodegarden.beecomb.common.executor.JobHandlerRegistrationBean.JobHandlerRegistration;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.beecomb.executor.registry.JobHandlerRegistry;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.zookeeper.NewZooKeeperListener;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.commons.zookeeper.exception.ExceedExpectedZooKeeperException;
import io.github.icodegarden.commons.zookeeper.exception.InvalidDataSizeZooKeeperException;
import io.github.icodegarden.commons.zookeeper.exception.ZooKeeperException;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceRegistry;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperRegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperJobHandlerRegistry implements JobHandlerRegistry, NewZooKeeperListener {
	private static final Logger log = LoggerFactory.getLogger(ZooKeeperJobHandlerRegistry.class);

	private AtomicInteger versionRef = new AtomicInteger();
	private Map<String/* name */, JobHandler> name_jobHandlers;

	private final String executorName;
	private final ZooKeeperHolder zooKeeperHolder;
	private final ZooKeeperInstanceRegistry zooKeeperInstanceRegistry;

	public ZooKeeperJobHandlerRegistry(String executorName, ZooKeeperHolder zooKeeperHolder,
			ZooKeeperInstanceRegistry zooKeeperInstanceRegistry) {
		this.executorName = executorName;
		this.zooKeeperHolder = zooKeeperHolder;
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
		jobHandlerRegistrationBean.setExecutorName(executorName);
		Set<JobHandlerRegistration> jobHandlerRegistrations = jobHandlers.stream().map(jobHandler -> {
			JobHandlerRegistration jobHandlerRegistration = new JobHandlerRegistrationBean.JobHandlerRegistration();
			jobHandlerRegistration.setJobHandlerName(jobHandler.name());
			return jobHandlerRegistration;
		}).collect(Collectors.toSet());
		jobHandlerRegistrationBean.setJobHandlerRegistrations(jobHandlerRegistrations);

		String json = JsonUtils.serialize(jobHandlerRegistrationBean);
		byte[] data = null;
		try {
			data = json.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
		if (data.length >= InvalidDataSizeZooKeeperException.MAX_DATA_SIZE) {
			throw new InvalidDataSizeZooKeeperException(data.length);
		}
		try {
			zooKeeperHolder.getConnectedZK().setData(instance.getZnode(), data, versionRef.getAndIncrement());
			name_jobHandlers = jobHandlers.stream().collect(Collectors.toMap(JobHandler::name, i -> i));
		} catch (KeeperException.NoNodeException ignore) {
			zooKeeperInstanceRegistry.deregister();
			ZooKeeperRegisteredInstance newInstance = zooKeeperInstanceRegistry.registerIfNot();
			try {
				Stat stat = zooKeeperHolder.getConnectedZK().exists(newInstance.getZnode(), false);
				versionRef.set(stat.getVersion());
				zooKeeperHolder.getConnectedZK().setData(newInstance.getZnode(), data, versionRef.getAndIncrement());
			} catch (KeeperException | InterruptedException e) {
				throw new ExceedExpectedZooKeeperException(
						String.format("ex on register jobHandlers after NoNodeException, znode [%s]", nodeName), e);
			}
		} catch (KeeperException.BadVersionException ignore) {
			try {
				Stat stat = zooKeeperHolder.getConnectedZK().exists(instance.getZnode(), false);
				versionRef.set(stat.getVersion());
				zooKeeperHolder.getConnectedZK().setData(instance.getZnode(), data, stat.getVersion());
			} catch (ZooKeeperException | KeeperException | InterruptedException e) {
				throw new ExceedExpectedZooKeeperException(
						String.format("ex on register jobHandlers znode [%s]", nodeName), e);
			}
		} catch (KeeperException | InterruptedException e) {
			throw new ExceedExpectedZooKeeperException(String.format("ex on register jobHandlers znode [%s]", nodeName),
					e);
		}
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

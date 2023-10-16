package io.github.icodegarden.beecomb.master.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.icodegarden.beecomb.master.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.master.discovery.InstanceDiscoveryListener;
import io.github.icodegarden.beecomb.master.service.InstanceRemoteService;
import io.github.icodegarden.beecomb.master.service.JobFacadeManager;
import io.github.icodegarden.nutrient.lang.concurrent.NamedThreadFactory;
import io.github.icodegarden.nutrient.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.nutrient.lang.metricsregistry.RegisteredInstance;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;
import io.github.icodegarden.nutrient.zookeeper.concurrent.lock.ZooKeeperLock;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 当感知Worker下线（或不可用等）时把任务重置为未队列
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Component
public class JobRecoveryListener implements InstanceDiscoveryListener {

	/**
	 * 单线程够用，极端情况排队
	 */
	private final ExecutorService threadpool = Executors
			.newSingleThreadExecutor(new NamedThreadFactory("JobRecovery-Listener"));

	@Autowired
	private InstanceProperties instanceProperties;
	@Autowired
	private CuratorFramework client;
	@Autowired
	private JobFacadeManager jobFacadeManager;
	@Autowired
	private InstanceRemoteService instanceRemoteService;

	/**
	 * 并发处理<br>
	 * 根据实例加锁<br>
	 * 
	 * @param registeredInstance
	 */
	@Override
	public void onInstanceDeleted(RegisteredInstance registeredInstance) {
		if (log.isInfoEnabled()) {
			log.info("Worker {}:{} maybe unhealthy, try to recovery jobs", registeredInstance.getIp(),
					registeredInstance.getPort());
		}
		threadpool.execute(() -> {
			DistributedLock lock = new ZooKeeperLock(client, instanceProperties.getZookeeper().getLockRoot(),
					"Worker-" + SystemUtils.formatIpPort(registeredInstance.getIp(), registeredInstance.getPort()));

			if (lock.acquire(1000)) {
				/**
				 * 为了确保真的不健康了，需要先测探
				 */
				try {
					boolean liveness = instanceRemoteService.isLiveness(registeredInstance.getIp(),
							registeredInstance.getPort());
					if (log.isInfoEnabled()) {
						log.info("Worker {}:{} really unhealthy", registeredInstance.getIp(),
								registeredInstance.getPort());
					}
					if (!liveness) {
						doRecovery(registeredInstance);
					}
				} finally {
					lock.release();
				}
			}
		});
	}

	private void doRecovery(RegisteredInstance registeredInstance) {
		String queuedAtInstance = SystemUtils.formatIpPort(registeredInstance.getIp(), registeredInstance.getPort());
		if (log.isInfoEnabled()) {
			log.info("recovery jobs queuedAtInstance:{}", queuedAtInstance);
		}
		int count = jobFacadeManager.recoveryThatNoQueuedActuallyByQueuedAtInstance(queuedAtInstance);
		if (log.isInfoEnabled()) {
			log.info("recovery jobs queuedAtInstance:{} count:{}", queuedAtInstance, count);
		}
	}

}

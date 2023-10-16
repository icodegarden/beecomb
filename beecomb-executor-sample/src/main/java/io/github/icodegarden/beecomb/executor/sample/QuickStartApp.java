package io.github.icodegarden.beecomb.executor.sample;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.github.icodegarden.beecomb.client.BeeCombClient;
import io.github.icodegarden.beecomb.client.ZooKeeperBeeCombClient;
import io.github.icodegarden.beecomb.client.ZooKeeperClientProperties;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateDelayJobDTO.Delay;
import io.github.icodegarden.beecomb.client.pojo.view.CreateJobVO;
import io.github.icodegarden.beecomb.client.security.Authentication;
import io.github.icodegarden.beecomb.client.security.BasicAuthentication;
import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import io.github.icodegarden.beecomb.executor.BeeCombExecutor;
import io.github.icodegarden.beecomb.executor.ZooKeeperSupportInstanceProperties;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.beecomb.executor.sample.handler.QuickStartJobHandler;
import io.github.icodegarden.nutrient.exchange.exception.ExchangeException;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class QuickStartApp {

	private static final String EXECUTOR_NAME = "QuickStartApp";

	/**
	 * 本例作为快速开始演示，在本例中Executor与Application是同一个应用
	 */
	public static void main(String[] args) throws IOException {
		/**
		 * 修改为你的地址
		 */
		String zkConnectString = "127.0.0.1:2181";
		
		ZooKeeper zookeeper = new ZooKeeper(zkConnectString);
		ZooKeeperSupportInstanceProperties properties = new ZooKeeperSupportInstanceProperties(zookeeper);
		BeeCombExecutor beeCombExecutor = BeeCombExecutor.start(EXECUTOR_NAME, properties);
		List<JobHandler> jobHandlers = Arrays.asList(new QuickStartJobHandler());
		beeCombExecutor.registerReplace(jobHandlers);

		/**
		 * 创建client
		 */
		Authentication authentication = new BasicAuthentication("beecomb", "beecomb");//client认证方式
		ZooKeeper zooKeeper = new ZooKeeper(zkConnectString);
		ZooKeeperClientProperties clientProperties = new ZooKeeperClientProperties(authentication, zooKeeper);
		BeeCombClient beeCombClient = new ZooKeeperBeeCombClient(clientProperties);

		try {
			/**
			 * 创建延迟任务，达到延迟后 {@link QuickStartAppJobHandler} 将触发任务执行
			 */
			Delay delay = new CreateDelayJobDTO.Delay(3000L);
			delay.setRetryOnNoQualified(3);//当没有合格的执行器时重试次数
			delay.setRetryOnExecuteFailed(3);//当执行失败时重试次数
			CreateDelayJobDTO job = new CreateDelayJobDTO("QuickStartDelayJob", EXECUTOR_NAME, QuickStartJobHandler.NAME,
					delay);
			CreateJobVO response = beeCombClient.createJob(job);
			if (response.getDispatchException() == null) {
				System.out.println(
						"创建示例任务成功，队列所在实例：" + response.getJob().getQueuedAtInstance()/* 若使用async方式，则该字段是null */);
			} else {
				System.out.println("创建示例任务成功，但分配队列失败：" + response.getDispatchException());
			}
		} catch (ExchangeException e) {
			log.error("创建示例任务失败", e);
		}

		/**
		 * 当需要关闭时
		 */
//		beeCombClient.close();
//		/**
//		 * 优雅停机
//		 */
//		beeCombExecutor.close();
	}

}

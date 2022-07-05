package io.github.icodegarden.beecomb.executor.sample;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.github.icodegarden.beecomb.client.BeeCombClient;
import io.github.icodegarden.beecomb.client.ZooKeeperBeeCombClient;
import io.github.icodegarden.beecomb.client.ZooKeeperClientProperties;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateDelayJobDTO.Delay;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateScheduleJobDTO;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateScheduleJobDTO.Schedule;
import io.github.icodegarden.beecomb.client.pojo.view.CreateJobVO;
import io.github.icodegarden.beecomb.client.security.Authentication;
import io.github.icodegarden.beecomb.client.security.BasicAuthentication;
import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import io.github.icodegarden.beecomb.executor.BeeCombExecutor;
import io.github.icodegarden.beecomb.executor.InstanceProperties;
import io.github.icodegarden.beecomb.executor.ZooKeeperSupportInstanceProperties;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.beecomb.executor.sample.handler.BizOnExpiredDelayJobHandler;
import io.github.icodegarden.beecomb.executor.sample.handler.ParallelJobHandler;
import io.github.icodegarden.beecomb.executor.sample.handler.ScheudleUntilSuccessScheduleJobHandler;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class SampleApp {

	private static final String EXECUTOR_NAME = "sampleExecutor";

	public static void main(String[] args) throws IOException {
		/**
		 * 如果是spring/springboot项目，可以在bean初始化阶段启动，具体取决于用户自己的项目情况
		 */
		String zkConnectString = "127.0.0.1:2181";/* 多个以,号分割 */
		BeeCombExecutor executor = startExecutor(zkConnectString);

		/**
		 * 使用sdk 调用openapi接口
		 */

		/**
		 * 选择一种认证方式
		 */
		Authentication authentication = new BasicAuthentication("beecomb", "beecomb");

		/**
		 * 这里client选择使用连接zookeeper的方式
		 */
		ZooKeeper zooKeeper = new ZooKeeper(zkConnectString);
		zooKeeper.setRoot("/beecomb"/* /beecomb是默认值，如果修改过则按实际 */);
		zooKeeper.setAclAuth("beecomb:beecomb");// 默认beecomb:beecomb

		ZooKeeperClientProperties clientProperties = new ZooKeeperClientProperties(authentication, zooKeeper);
		BeeCombClient beeCombClient = new ZooKeeperBeeCombClient(clientProperties);

		/**
		 * 如果使用nginx等对masters进行了代理，可以直接指定代理的地址 <br>
		 * UrlsClientProperties urlsClientProperties = new
		 * UrlsClientProperties(authentication, Arrays.asList("http://ip:port"));
		 * BeeCombClient beeCombClient = new UrlsBeeCombClient(urlsClientProperties);
		 */

		for (;;) {
			try {
				System.out.println("请输入需要演示的任务，1=红包自动退款场景，2=抢票场景，3=任务分片并行处理场景");
				int read = System.in.read();
				if (read == '1') {
					bizOnExpiredDelayJob(beeCombClient);
				}
				if (read == '2') {
					scheudleUntilSuccessScheduleJob(beeCombClient);
				}
				if (read == '3') {
					parallelJobHandler(beeCombClient);
				}

				if (read == 'e') {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 进程退出前，执行优雅停机
		 */
		executor.close();
		System.out.println("退出");
	}

	private static void bizOnExpiredDelayJob(BeeCombClient beeCombClient) {
		int packetId = new Random().nextInt(Integer.MAX_VALUE);

		Delay delay = new CreateDelayJobDTO.Delay(3000L);
		delay.setRetryOnExecuteFailed(3);// 任务执行失败时重试次数
		delay.setRetryBackoffOnExecuteFailed(3000);
		delay.setRetryOnNoQualified(3);// 没有合格的Executor时重试次数
		delay.setRetryBackoffOnNoQualified(10000);
		CreateDelayJobDTO job = new CreateDelayJobDTO("testBizOnExpired" + packetId, EXECUTOR_NAME,
				BizOnExpiredDelayJobHandler.NAME, delay);
		job.setUuid("biz_packct_" + packetId);

		CreateJobVO response = beeCombClient.createJob(job);
		pringResponse(response);
	}

	private static void scheudleUntilSuccessScheduleJob(BeeCombClient beeCombClient) {
		Schedule schedule = CreateScheduleJobDTO.Schedule.sheduleCron("1/3 * * * * *");
		CreateScheduleJobDTO job = new CreateScheduleJobDTO("testScheudleUntilSuccess", EXECUTOR_NAME,
				ScheudleUntilSuccessScheduleJobHandler.NAME, schedule);
		job.setParams("{}");// json

		CreateJobVO response = beeCombClient.createJob(job);
		pringResponse(response);
	}

	private static void parallelJobHandler(BeeCombClient beeCombClient) {
		Schedule schedule = CreateScheduleJobDTO.Schedule.sheduleCron("1/3 * * * * *");
		CreateScheduleJobDTO job = new CreateScheduleJobDTO("testParallelJob", EXECUTOR_NAME, ParallelJobHandler.NAME,
				schedule);
		job.setParallel(true);
		job.setMaxParallelShards(8);

		CreateJobVO response = beeCombClient.createJob(job);
		pringResponse(response);
	}

	private static void pringResponse(CreateJobVO response) {
		if (response.getDispatchException() == null) {
			System.out.println("创建示例任务成功，队列所在实例：" + response.getJob().getQueuedAtInstance()/* 若使用async方式，则该字段是null */);
		} else {
			System.out.println("创建示例任务成功，但分配队列失败：" + response.getDispatchException());
		}
	}

	private static BeeCombExecutor startExecutor(String zkConnectString) {
		ZooKeeper zookeeper = new ZooKeeper(zkConnectString);
		ZooKeeperSupportInstanceProperties properties = new ZooKeeperSupportInstanceProperties(zookeeper);
		InstanceProperties.Server server = new InstanceProperties.Server();
		server.setExecutorPort(new Random().nextInt(10000) + 10000);// 默认不需要修改
		properties.setServer(server);
		BeeCombExecutor beeCombExecutor = BeeCombExecutor.start(EXECUTOR_NAME, properties);

		List<JobHandler> jobHandlers = Arrays.asList(new BizOnExpiredDelayJobHandler(),
				new ScheudleUntilSuccessScheduleJobHandler(), new ParallelJobHandler());

		beeCombExecutor.registerReplace(jobHandlers);

		return beeCombExecutor;
	}
}

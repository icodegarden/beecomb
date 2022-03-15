package io.github.icodegarden.beecomb.executor.sample;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.github.icodegarden.beecomb.client.BeeCombClient;
import io.github.icodegarden.beecomb.client.UrlsBeeCombClient;
import io.github.icodegarden.beecomb.client.UrlsClientProperties;
import io.github.icodegarden.beecomb.client.ZooKeeperBeeCombClient;
import io.github.icodegarden.beecomb.client.ZooKeeperClientProperties;
import io.github.icodegarden.beecomb.client.pojo.response.CreateJobResponse;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateDelayJobDTO.Delay;
import io.github.icodegarden.beecomb.client.security.Authentication;
import io.github.icodegarden.beecomb.client.security.BasicAuthentication;
import io.github.icodegarden.beecomb.executor.BeeCombExecutor;
import io.github.icodegarden.beecomb.executor.ZooKeeperSupportInstanceProperties;
import io.github.icodegarden.beecomb.executor.ZooKeeperSupportInstanceProperties.ZooKeeper;
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
		BeeCombExecutor startExecutor = startExecutor();

		/**
		 * 使用sdk 调用openapi接口
		 */

		/**
		 * 选择一种认证方式
		 */
		Authentication authentication = new BasicAuthentication("beecomb", "beecomb");

		/**
		 * 连接zookeeper的方式
		 */
		ZooKeeperClientProperties clientProperties = new ZooKeeperClientProperties(authentication,
				new ZooKeeperClientProperties.ZooKeeper("/beecomb"/* /beecomb是默认值，如果修改过则按实际 */,
						"192.168.80.128:2181"/* 多个以,号分割 */, 3000, 3000));
		BeeCombClient beeCombClient = new ZooKeeperBeeCombClient(clientProperties);

		/**
		 * 如果使用nginx等对masters进行了代理，可以直接指定代理的地址 UrlsClientProperties urlsClientProperties
		 * = new UrlsClientProperties(authentication, Arrays.asList("http://ip:port"));
		 * BeeCombClient beeCombClient = new UrlsBeeCombClient(urlsClientProperties);
		 */

		for (;;) {
			try {
				System.out.println("请输入指令：");
				int read = System.in.read();
				if (read == '1') {
					int packetId = new Random().nextInt(100000);
					
					Delay delay = new CreateDelayJobDTO.Delay(3000);
					delay.setRetryOnExecuteFailed(3);//任务执行失败时重试次数
					delay.setRetryBackoffOnExecuteFailed(3000);
					delay.setRetryOnNoQualified(3);//没有合格的Executor时重试次数
					delay.setRetryBackoffOnNoQualified(10000);
					CreateDelayJobDTO job = new CreateDelayJobDTO("testBizOnExpired"+packetId, EXECUTOR_NAME,
							BizOnExpiredDelayJobHandler.NAME, delay);
					job.setUuid("biz_packct_" + packetId);

					CreateJobResponse response = beeCombClient.createJob(job);
					if (response.getDispatchException() == null) {
						System.out.println(
								"创建 BizOnExpiredDelayJobHandler 示例任务成功，队列所在实例：" + response.getJob().getQueuedAtInstance());
					} else {
						System.out.println("创建 BizOnExpiredDelayJobHandler 示例任务成功：" + response + ", 但分配队列失败："
								+ response.getDispatchException());
					}
				}
				if (read == '2') {
//					beeCombClient.createJob(job);
//					System.out.println("创建 ScheudleUntilSuccessScheduleJobHandler 示例任务成功："+response);
				}
				if (read == '3') {
//					beeCombClient.createJob(job);
//					System.out.println("创建 ParallelJobHandler 示例任务成功："+response);
				}

				if (read == 'e') {
					break;
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 进程退出前，执行优雅停机
		 */
		startExecutor.close();
		System.out.println("退出");
	}

	private static BeeCombExecutor startExecutor() {
		ZooKeeper zookeeper = new ZooKeeperSupportInstanceProperties.ZooKeeper("192.168.80.128:2181");
		ZooKeeperSupportInstanceProperties properties = new ZooKeeperSupportInstanceProperties(zookeeper);
		BeeCombExecutor beeCombExecutor = BeeCombExecutor.start(EXECUTOR_NAME, properties);

		List<JobHandler> jobHandlers = Arrays.asList(new BizOnExpiredDelayJobHandler(),
				new ScheudleUntilSuccessScheduleJobHandler(), new ParallelJobHandler());

		beeCombExecutor.registerReplace(jobHandlers);

		return beeCombExecutor;
	}
}

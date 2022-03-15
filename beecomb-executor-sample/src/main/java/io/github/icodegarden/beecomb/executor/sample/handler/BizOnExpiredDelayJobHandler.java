package io.github.icodegarden.beecomb.executor.sample.handler;

import java.io.IOException;
import java.util.Random;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;

/**
 * <h1>JobHandler能处理Delay还是Schedule任务，是由job的executorName、jobHandlerName一起决定的，JobHandler自身并不区分处理哪种类型</h1>
 * 
 * 到期自动处理业务的示例，适用于： <br>
 * 红包到期未领自动退款<br>
 * 购物车商品到期未支付自动重新上架<br>
 * 商品租赁到期自动回收<br>
 * 延迟发送指令给设备<br>
 * 
 * 等 数量庞大、各对象有各自的延迟时间 的场景
 * 
 * @author Fangfang.Xu
 *
 */
public class BizOnExpiredDelayJobHandler implements JobHandler {

	public static final String NAME = "BizOnExpired";
	
	@Override
	public String name() {
		return NAME;
	}

	/**
	 * 这里只表示红包自动退款的处理
	 */
	@Override
	public ExecuteJobResult handle(Job job) throws Exception {
		/**
		 * 获取红包id，这里在业务上约定job的uuid = "biz_packct_" + 红包的id
		 */
		String uuid = job.getUuid();
		String packctIdStr = uuid.substring("biz_packct_".length(), uuid.length());
		Long packctId = Long.valueOf(packctIdStr);

		/**
		 * 退款
		 */
		refund(packctId);// 抛出任务异常表示处理失败

		return new ExecuteJobResult();// 成功
	}

	private void refund(Long packctId) throws Exception {
		/**
		 * 检查是否已退款，是则不再处理
		 */

		/**
		 * 模拟有几率发生异常
		 */
		Random random = new Random();
		if (random.nextInt(3) == 0) {
			throw new IOException("数据入库失败");
		}

		/**
		 * 处理成功
		 */
		System.out.println("退款成功");
	}

}

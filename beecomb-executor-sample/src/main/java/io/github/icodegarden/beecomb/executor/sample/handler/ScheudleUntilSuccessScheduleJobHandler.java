package io.github.icodegarden.beecomb.executor.sample.handler;

import java.util.List;
import java.util.Random;

import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.nutrient.lang.util.JsonUtils;
import lombok.Data;

/**
 * <h1>JobHandler能处理Delay还是Schedule任务，是由job的executorName、jobHandlerName一起决定的，JobHandler自身并不区分处理哪种类型</h1>
 * 
 * 一直调度直到完全处理成功的示例，适用于： <br>
 * 抢票（每个抢票任务有自己的开始时间、抢票频率、）<br>
 * 分批处理大批量数据（单批处理容易产生问题）<br>
 * 
 * 
 * 等等场景
 * 
 * @author Fangfang.Xu
 *
 */
public class ScheudleUntilSuccessScheduleJobHandler implements JobHandler {
	
	public static final String NAME = "ScheudleUntilSuccess";
	
	@Override
	public String name() {
		return "ScheudleUntilSuccess";
	}

	@Override
	public ExecuteJobResult handle(Job job) throws Exception {
		return ticketGrabbing(job);// 抢票

//		return bigData(job);//分批处理大批量数据
	}

	/**
	 * 表示抢票
	 */
	private ExecuteJobResult ticketGrabbing(Job job) {
		/**
		 * 获取用户信息和购票参数
		 */
		String params = job.getParams();
		Assert.hasText(params, "Missing:params");
		TicketGrabbingParams ticketGrabbingParams = JsonUtils.deserialize(params, TicketGrabbingParams.class);

		/**
		 * 使用 ticketGrabbingParams 购票，这里表示有几率买到
		 */
		Random random = new Random();
		if (random.nextInt(5) == 0) {
			System.out.println("抢票成功");
			ExecuteJobResult executeJobResult = new ExecuteJobResult();
			/**
			 * 成功了，结束调度
			 */
			executeJobResult.setEnd(true);
			return executeJobResult;
		}

		/**
		 * 票没买成功，但是整个执行是成功的，因此不抛出异常<br>
		 * 由于任务没有end，下次依然会进行调度
		 */
		System.out.println("抢票失败，等待下次触发");
		ExecuteJobResult executeJobResult = new ExecuteJobResult();
		executeJobResult.setExecuteReturns("Ticket Grabbing Failed");
		return executeJobResult;
	}

	/**
	 * 以下代码仅表示场景，并不代表sample代码一定正确执行
	 * @param job
	 * @return
	 */
	private ExecuteJobResult bigData(Job job) {
		/**
		 * 假设这个大任务需要处理100万个用户数据，我们采用每次调度只处理1000个用户<br>
		 * 
		 */

		/**
		 * 获取任务参数，这里任务参数我们表示所有要处理的用户数据的查询条件
		 */
		String params = job.getParams();
		BigDataParams queryParams = JsonUtils.deserialize(params, BigDataParams.class);

		/**
		 * 获取上次已处理到哪个用户id，lastExecuteReturns是每次调度处理完成的返回结果
		 */
		String lastExecuteReturns = job.getLastExecuteReturns();
		Long userIdSearchAfter = lastExecuteReturns == null ? 0L : Long.valueOf(lastExecuteReturns);

		/**
		 * 使用queryParams + userIdSearchAfter的组合查询1000个要处理的数据
		 */
		List<Object> userDatas = searchUserDatas(queryParams, userIdSearchAfter, 100);

		/**
		 * 处理userDatas
		 */
		System.out.println("这一批处理成功");

		/**
		 * 已经全部处理完毕，结束任务
		 */
		if (userDatas == null || userDatas.size() < 100) {
			ExecuteJobResult executeJobResult = new ExecuteJobResult();
			executeJobResult.setEnd(true);
			return executeJobResult;
		}

		/**
		 * 返回本次调度结果
		 */
		ExecuteJobResult executeJobResult = new ExecuteJobResult();
		/**
		 * 把本次处理到的用户id作为executeReturns，供下次调度使用
		 */
		Long userId = new Random().nextLong();
		executeJobResult.setExecuteReturns(userId.toString());
		return executeJobResult;
	}

	private List<Object> searchUserDatas(BigDataParams queryParams, Long userIdSearchAfter, int size) {
		return null;
	}

	/**
	 * 抢票参数
	 */
	@Data
	public static class TicketGrabbingParams {
		// 省略字段
	}

	/**
	 * 大任务处理参数
	 */
	@Data
	public static class BigDataParams {
		// 省略字段
	}
}

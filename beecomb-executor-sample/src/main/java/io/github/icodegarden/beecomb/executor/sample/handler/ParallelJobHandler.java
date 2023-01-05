package io.github.icodegarden.beecomb.executor.sample.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.common.executor.ScheduleJob;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;

/**
 * <h1>JobHandler能处理Delay还是Schedule任务，是由job的executorName、jobHandlerName一起决定的，JobHandler自身并不区分处理哪种类型</h1>
 * 
 * 并行分片处理的示例，适用于： <br>
 * 数据量大，要求在一段时间内完成的大批量计算任务<br>
 * 
 * 
 * <h1>并行任务既可以是Delay类型、也可以是Schedule类型，区别可以看本例</h1>
 * 
 * @author Fangfang.Xu
 *
 */
public class ParallelJobHandler implements JobHandler {

	public static final String NAME = "Parallel";

	static List<Long> datas = new ArrayList<Long>();
	static {
		for (long i = 0; i < 10; i++) {
			datas.add(i);
		}
	}

	@Override
	public String name() {
		return "Parallel";
	}

	@Override
	public ExecuteJobResult handle(Job job) throws Exception {
		/**
		 * 本次任务执行中，本实例对应的分片number，0表示第一个分片
		 */
		int shard = job.getShard();
		/**
		 * 本次任务执行中，共有shardTotal个分片实例
		 */
		int shardTotal = job.getShardTotal();

		/**
		 * 使用shard 、 shardTotal获取本实例应该处理的分片数据
		 */
		List<Long> currentShardDatas = datas.stream().filter(i -> i % shardTotal == shard).collect(Collectors.toList());
		System.out.println(
				String.format("shard:%d, shardTotal:%s, currentShardDatas:%s", shard, shardTotal, currentShardDatas));

		Random random = new Random();
		if (random.nextInt(5) == 0) {
			/**
			 * 只要有任意的分片处理失败，本次任务执行就视为失败<br>
			 * 区别：<br>
			 * Scheudle类型下次会继续调度<br>
			 * Delay类型如果失败次数没到阈值下次也会再触发
			 */
			throw new IOException("分片"+shard+"处理异常");
		}

		System.out.println("分片"+shard+"处理成功");
		
		ExecuteJobResult executeJobResult = new ExecuteJobResult();
		/**
		 * 设置true当所有分片都成功后，将会触发onParallelSuccess方法的回调（只会有一个executor收到回调）
		 */
		executeJobResult.setOnParallelSuccessCallback(true);
		
		/**
		 * 区别：<br>
		 * Scheudle类型：如果 所有的 分片的ExecuteJobResult的end没有设置为true，则下次会继续 并行的 调度<br>
		 * Delay类型：任务将会结束
		 */
		if (job instanceof ScheduleJob) {
			executeJobResult.setEnd(true);
			return executeJobResult;
		}
		// Delay
		return executeJobResult;
	}

	@Override
	public void onParallelSuccess(Job job) throws Exception {
		System.out.println("Parallel 所有分片执行成功, job:" + job);
	}
}

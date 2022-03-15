package io.github.icodegarden.beecomb.executor.sample.handler;

import java.io.IOException;
import java.util.Random;

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
	
	@Override
	public String name() {
		return "Parallel";
	}

	@Override
	public ExecuteJobResult handle(Job job) throws Exception {
		/**
		 * 本次任务执行中，本实例对应的分片number
		 */
		int shard = job.getShard();
		/**
		 * 本次任务执行中，共有shardTotal个分片实例
		 */
		int shardTotal = job.getShardTotal();

		/**
		 * 使用shard 、 shardTotal处理本实例对应的分片数据
		 */
		Random random = new Random();
		if (random.nextInt(5) == 0) {
			/**
			 * 只要有任意的分片处理失败，本次任务执行就视为失败<br>
			 * 区别：<br>
			 * Scheudle类型下次会继续调度<br>
			 * Delay类型如果失败次数没到阈值下次也会再触发
			 */
			throw new IOException("本分片处理异常");
		}

		System.out.println("处理成功");

		/**
		 * 区别：<br>
		 * Scheudle类型：如果 所有的 分片的ExecuteJobResult的end没有设置为true，则下次会继续 并行的 调度<br>
		 * Delay类型：任务将会结束
		 */
		if(job instanceof ScheduleJob) {
			ExecuteJobResult executeJobResult = new ExecuteJobResult();
			executeJobResult.setEnd(true);
			return executeJobResult;
		}
		//Delay
		return new ExecuteJobResult();
	}

}

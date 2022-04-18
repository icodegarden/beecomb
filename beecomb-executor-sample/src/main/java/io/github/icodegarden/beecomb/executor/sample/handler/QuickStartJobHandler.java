package io.github.icodegarden.beecomb.executor.sample.handler;

import java.util.Random;

import io.github.icodegarden.beecomb.common.executor.DelayJob;
import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class QuickStartJobHandler implements JobHandler {
	public static final String NAME = "QuickStartAppJobHandler";

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public ExecuteJobResult handle(Job job) throws Exception {
		System.out.println("handle job:" + job);

		if (job instanceof DelayJob) {
			int delay = ((DelayJob) job).getDelay();
			System.out.println(delay);
		}

		if (new Random().nextInt(3) == 0) {
			return new ExecuteJobResult();// 执行成功
		} else {
			throw new Exception("执行失败");
		}
	}
}
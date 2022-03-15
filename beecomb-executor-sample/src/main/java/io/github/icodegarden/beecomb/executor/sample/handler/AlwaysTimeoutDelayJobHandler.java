package io.github.icodegarden.beecomb.executor.sample.handler;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class AlwaysTimeoutDelayJobHandler implements JobHandler {

	@Override
	public String name() {
		return "alwaysTimeoutDelay";
	}

	@Override
	public ExecuteJobResult handle(Job job) {
		System.out.println("alwaysTimeoutDelay done");
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
		}
		return new ExecuteJobResult();
	}

}

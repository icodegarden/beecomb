package io.github.icodegarden.beecomb.executor.sample.handler;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class AlwaysFailDelayJobHandler implements JobHandler {

	@Override
	public String name() {
		return "alwaysFailDelay";
	}

	@Override
	public ExecuteJobResult handle(Job job) {
		System.out.println("alwaysFailDelay done");
		throw new RuntimeException("always fail");
	}

}

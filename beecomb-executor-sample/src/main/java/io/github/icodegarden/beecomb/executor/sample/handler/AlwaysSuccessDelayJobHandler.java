package io.github.icodegarden.beecomb.executor.sample.handler;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class AlwaysSuccessDelayJobHandler implements JobHandler {

	@Override
	public String name() {
		return "alwaysSuccessDelay";
	}

	@Override
	public ExecuteJobResult handle(Job job) {
		System.out.println("alwaysSuccessDelay done");
		ExecuteJobResult executeJobResult = new ExecuteJobResult();
		executeJobResult.setExecuteReturns("alwaysSuccessDelay");
		return executeJobResult;
	}

}

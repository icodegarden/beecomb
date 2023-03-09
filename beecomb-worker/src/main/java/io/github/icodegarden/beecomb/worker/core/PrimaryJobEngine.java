package io.github.icodegarden.beecomb.worker.core;

import java.util.Map;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.commons.lang.result.Result3;
import io.github.icodegarden.commons.springboot.SpringContext;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Primary
@Service
public class PrimaryJobEngine implements JobEngine {

	@Override
	public String shutdownName() {
		return "primary";
	}

	private JobEngine getJobEngine(ExecutableJobBO job) {
		String typeName = job.getType().name().toLowerCase();
		JobEngine jobEngine = SpringContext.getApplicationContext().getBean(typeName, JobEngine.class);
		return jobEngine;
	}

	@Override
	public boolean allowEnQueue(ExecutableJobBO job) {
		JobEngine jobEngine = getJobEngine(job);
		return jobEngine.allowEnQueue(job);
	}

	@Override
	public Result3<ExecutableJobBO, ? extends Object, JobEngineException> enQueue(ExecutableJobBO job) {
		JobEngine jobEngine = getJobEngine(job);
		return jobEngine.enQueue(job);
	}

	@Override
	public boolean removeQueue(ExecutableJobBO job) {
		JobEngine jobEngine = getJobEngine(job);
		return jobEngine.removeQueue(job);
	}
	
	@Override
	public boolean run(ExecutableJobBO job) {
		JobEngine jobEngine = getJobEngine(job);
		return jobEngine.run(job);
	}

	@Override
	public int queuedSize() {
		Map<String, JobEngine> beansOfType = SpringContext.getApplicationContext().getBeansOfType(JobEngine.class);
		return beansOfType.values().stream().filter(je -> {
			return !(je.getClass() == PrimaryJobEngine.class
					|| je.getClass().getSuperclass() == PrimaryJobEngine.class);
		}).mapToInt(JobEngine::queuedSize).sum();
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void shutdownBlocking(long blockTimeoutMillis) {
	}
}

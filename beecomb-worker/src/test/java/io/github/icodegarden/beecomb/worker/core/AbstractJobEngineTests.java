package io.github.icodegarden.beecomb.worker.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.test.PropertiesConfig;
import io.github.icodegarden.beecomb.worker.core.JobEngine.JobTrigger;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.commons.lang.metrics.MetricsOverload;
import io.github.icodegarden.commons.lang.result.Result3;
import io.github.icodegarden.commons.lang.result.Results;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class AbstractJobEngineTests extends PropertiesConfig {

	MetricsOverload metricsOverload = mock(MetricsOverload.class);
	
	AbstractJobEngine abstractJobEngine;
	@BeforeEach
	void init() {
		abstractJobEngine = mock(AbstractJobEngine.class);
		abstractJobEngine.metricsOverload = metricsOverload;
	}
	
	AbstractJobEngine getJobEngine() {
		return abstractJobEngine;
	}
	
	ExecutableJobBO getJob() {
		ExecutableJobBO job = new ExecutableJobBO();
		job.setType(JobType.Delay);
		DelayBO delay = new DelayBO();
		delay.setDelay(1000);
		job.setDelay(delay);
		return job;
	}

	@Test
	void allowEnQueue() {
		ExecutableJobBO job = getJob();
		job.setWeight(3);

		when(getJobEngine().allowEnQueue(job)).thenCallRealMethod();
		
		getJobEngine().allowEnQueue(job);
		
		verify(metricsOverload, times(1)).willOverload(job);
		
	}

	/**
	 * weight3超载
	 */
	@Test
	void enQueueFailOn_WeightExceed() {
		ExecutableJobBO job = getJob();
		job.setWeight(3);

		when(getJobEngine().enQueue(job)).thenCallRealMethod();
		doReturn(true).when(metricsOverload).willOverload(job);
		
		Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueueResult = getJobEngine().enQueue(job);
		assertThat(enQueueResult.isSuccess()).isFalse();

	}

	@Test
	void enQueueFailOn_DoEnQeuueReturnFail() {
		ExecutableJobBO job = getJob();
		job.setWeight(2);

		when(getJobEngine().enQueue(job)).thenCallRealMethod();
		doReturn(Results.of(false, job, null, null)).when(getJobEngine()).doEnQueue(any());

		Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueueResult = getJobEngine().enQueue(job);
		assertThat(enQueueResult.isSuccess()).isFalse();

	}

	@Test
	void enQueueFailOn_FlushMetricsFail() {
		ExecutableJobBO job = getJob();
		job.setWeight(2);

		doThrow(RuntimeException.class).when(metricsOverload).flushMetrics();
		when(getJobEngine().enQueue(job)).thenCallRealMethod();
		
		Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueueResult = getJobEngine().enQueue(job);
		assertThat(enQueueResult.isSuccess()).isFalse();

	}

	@Test
	void enQueueSuccess() {
		ExecutableJobBO job = getJob();
		job.setWeight(1);

		doReturn(true).when(metricsOverload).incrementOverload(job);
		when(getJobEngine().enQueue(job)).thenCallRealMethod();
		doReturn(Results.of(true, job, null, null)).when(getJobEngine()).doEnQueue(any());
		
		Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueueResult1 = getJobEngine().enQueue(job);
		assertThat(enQueueResult1.isSuccess()).isTrue();
	}

}

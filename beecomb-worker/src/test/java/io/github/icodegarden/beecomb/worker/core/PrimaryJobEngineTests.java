package io.github.icodegarden.beecomb.worker.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.worker.core.DelayJobEngine;
import io.github.icodegarden.beecomb.worker.core.ScheduleJobEngine;
import io.github.icodegarden.beecomb.worker.core.JobEngine.JobTrigger;
import io.github.icodegarden.beecomb.worker.exception.JobEngineException;
import io.github.icodegarden.commons.lang.result.Result3;
import io.github.icodegarden.commons.lang.result.Results;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class PrimaryJobEngineTests {

	@Autowired
	PrimaryJobEngine primaryJobEngine;
	@MockBean(name = "delay")
	DelayJobEngine delayJobEngine;
	@MockBean(name = "schedule")
	ScheduleJobEngine scheduleJobEngine;

	@Test
	void allowEnQueue() {
		ExecutableJobBO delayJob = new ExecutableJobBO();
		delayJob.setType(JobType.Delay);
		primaryJobEngine.allowEnQueue(delayJob);

		verify(delayJobEngine, times(1)).allowEnQueue(delayJob);
		verify(scheduleJobEngine, times(0)).allowEnQueue(delayJob);

		// --------------------------------------------------
		ExecutableJobBO scheduleJob = new ExecutableJobBO();
		scheduleJob.setType(JobType.Schedule);
		primaryJobEngine.allowEnQueue(scheduleJob);

		verify(delayJobEngine, times(0)).allowEnQueue(scheduleJob);
		verify(scheduleJobEngine, times(1)).allowEnQueue(scheduleJob);
	}

	@Test
	void enQueue() {
		ExecutableJobBO delayJob = new ExecutableJobBO();
		delayJob.setType(JobType.Delay);
		primaryJobEngine.enQueue(delayJob);

		verify(delayJobEngine, times(1)).enQueue(delayJob);
		verify(scheduleJobEngine, times(0)).enQueue(delayJob);

		// --------------------------------------------------
		ExecutableJobBO scheduleJob = new ExecutableJobBO();
		scheduleJob.setType(JobType.Schedule);
		primaryJobEngine.enQueue(scheduleJob);

		verify(delayJobEngine, times(0)).enQueue(scheduleJob);
		verify(scheduleJobEngine, times(1)).enQueue(scheduleJob);
	}

	@Test
	void removeQueue() {
		ExecutableJobBO delayJob = new ExecutableJobBO();
		delayJob.setType(JobType.Delay);
		Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueueResult = Results.of(true, delayJob, null, null);
		primaryJobEngine.removeQueue(enQueueResult);

		verify(delayJobEngine, times(1)).removeQueue(enQueueResult);
		verify(scheduleJobEngine, times(0)).removeQueue(enQueueResult);

		// --------------------------------------------------
		ExecutableJobBO scheduleJob = new ExecutableJobBO();
		scheduleJob.setType(JobType.Schedule);
		Result3<ExecutableJobBO, JobTrigger, JobEngineException> enQueueResult2 = Results.of(true, scheduleJob, null, null);
		primaryJobEngine.removeQueue(enQueueResult2);

		verify(delayJobEngine, times(0)).removeQueue(enQueueResult2);
		verify(scheduleJobEngine, times(1)).removeQueue(enQueueResult2);
	}

	@Test
	void queuedSize() {
		doReturn(1).when(delayJobEngine).queuedSize();
		doReturn(2).when(scheduleJobEngine).queuedSize();
		int queuedSize = primaryJobEngine.queuedSize();
		assertThat(queuedSize).isEqualTo(3);
	}
}

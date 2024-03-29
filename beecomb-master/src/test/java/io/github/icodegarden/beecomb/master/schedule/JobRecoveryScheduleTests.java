package io.github.icodegarden.beecomb.master.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.manager.JobRecoveryRecordManager;
import io.github.icodegarden.beecomb.master.service.JobFacadeManager;
import io.github.icodegarden.beecomb.master.service.WorkerRemoteService;
import io.github.icodegarden.nutrient.lang.concurrent.lock.DistributedLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JobRecoveryScheduleTests {

	@Test
	void start() throws Exception {
		DistributedLock lock = mock(DistributedLock.class);
		JobFacadeManager jobFacadeManager = mock(JobFacadeManager.class);
		WorkerRemoteService jobRemoteService = mock(WorkerRemoteService.class);
		JobRecoveryRecordManager jobRecoveryRecordService = mock(JobRecoveryRecordManager.class);

		// recoveryThatNoQueuedActually部分------------------------------------------
		doReturn(true).when(lock).acquire(anyLong());// 获取锁成功
		doReturn(true).when(lock).isAcquired();
		doReturn(true).when(jobFacadeManager).hasNoQueuedActually(any());// 探测到有需要恢复的
		doReturn(Arrays.asList(new ExecutableJobBO()), Collections.emptyList()).when(jobFacadeManager)
				.listJobsShouldRecovery(0, 10);// 第一次获取到有需要恢复的，第二次没有了

		JobRecoverySchedule jobRecovery = new JobRecoverySchedule(lock, jobFacadeManager, jobRemoteService, jobRecoveryRecordService);
		boolean start = jobRecovery.scheduleWithFixedDelay(1000, 1000);

		Assertions.assertThat(start).isTrue();

		Thread.sleep(1500);// 等待调度触发1次
		verify(lock, times(1)).acquire(anyLong());
		verify(jobFacadeManager, times(1)).hasNoQueuedActually(any());
		verify(jobFacadeManager, times(1)).recoveryThatNoQueuedActually(any());

		// listJobsShouldRecovery部分------------------------------------------
		verify(jobFacadeManager, atLeast(1)).listJobsShouldRecovery(0, 10);
		verify(jobRemoteService, times(1)).enQueue(any());
		verify(jobRecoveryRecordService, times(1)).createOrUpdate(any());

		// ------------------------------------------
		start = jobRecovery.scheduleWithFixedDelay(1000, 1000);
		Assertions.assertThat(start).isFalse();// 再start无效

		jobRecovery.close();
	}
}

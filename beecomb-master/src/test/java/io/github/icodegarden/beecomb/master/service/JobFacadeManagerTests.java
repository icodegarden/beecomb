package io.github.icodegarden.beecomb.master.service;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.service.JobFacadeManager;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class JobFacadeManagerTests {

	@Autowired
	private JobFacadeManager jobFacadeManager;
	@Autowired
	private JobMainMapper jobMainMapper;
	
	@Test
	void hasNoQueuedActually() {
		LocalDateTime time = LocalDateTime.now().minusSeconds(60);
		boolean b = jobFacadeManager.hasNoQueuedActually(time);
		Assertions.assertThat(b).isFalse();

		JobMainPO j1 = new JobMainPO();
		j1.setCreatedBy("FangfangXu");
		j1.setName("j1");
		j1.setType(JobType.Delay);
		j1.setExecutorName("1");
		j1.setJobHandlerName("1");
		j1.setNextTrigAt(time);
		jobMainMapper.add(j1);

		b = jobFacadeManager.hasNoQueuedActually(LocalDateTime.now());
		Assertions.assertThat(b).isTrue();
	}

	@Test
	void recoveryThatNoQueuedActually() {
		LocalDateTime time = LocalDateTime.now().minusSeconds(60);
		
		JobMainPO j1 = new JobMainPO();
		j1.setCreatedBy("FangfangXu");
		j1.setName("j1");
		j1.setType(JobType.Delay);
		j1.setExecutorName("1");
		j1.setJobHandlerName("1");
		j1.setNextTrigAt(time);
		jobMainMapper.add(j1);
		
		LocalDateTime nextTrigAtLt = LocalDateTime.now();
		int count = jobFacadeManager.recoveryThatNoQueuedActually(nextTrigAtLt);
		Assertions.assertThat(count).isGreaterThan(0);
	}
	
	@Test
	void recoveryThatNoQueuedActuallyByQueuedAtInstance() {
		String queuedAtInstance = "1.1.1.1:19898";
		
		JobMainPO j1 = new JobMainPO();
		j1.setCreatedBy("FangfangXu");
		j1.setName("j1");
		j1.setType(JobType.Delay);
		j1.setExecutorName("1");
		j1.setJobHandlerName("1");
		j1.setNextTrigAt(LocalDateTime.now());
		j1.setQueuedAtInstance(queuedAtInstance);
		jobMainMapper.add(j1);
		
		int count = jobFacadeManager.recoveryThatNoQueuedActuallyByQueuedAtInstance(queuedAtInstance);
		Assertions.assertThat(count).isGreaterThan(0);
	}

	@Test
	void listJobsShouldRecovery() {
		String queuedAtInstance = "1.1.1.1:19898";
		
		JobMainPO j1 = new JobMainPO();
		j1.setCreatedBy("FangfangXu");
		j1.setName("j1");
		j1.setType(JobType.Delay);
		j1.setExecutorName("1");
		j1.setJobHandlerName("1");
		j1.setEnd(true);// 完成的不需要恢复
		j1.setQueuedAtInstance(queuedAtInstance);
		jobMainMapper.add(j1);

		JobMainPO j2 = new JobMainPO();
		j2.setCreatedBy("FangfangXu");
		j2.setName("j2");
		j2.setType(JobType.Schedule);
		j2.setExecutorName("2");
		j2.setJobHandlerName("2");
		j2.setQueued(true);
		j2.setEnd(true);// 完成的不需要恢复
		j2.setQueuedAtInstance(queuedAtInstance);
		jobMainMapper.add(j2);

		JobMainPO j3 = new JobMainPO();
		j3.setCreatedBy("FangfangXu");
		j3.setName("j3");
		j3.setType(JobType.Delay);
		j3.setExecutorName("3");
		j3.setJobHandlerName("3");
		j3.setEnd(false);// 需要恢复
		j3.setPriority(1);// 低优先
		j3.setQueuedAtInstance(queuedAtInstance);
		jobMainMapper.add(j3);

		JobMainPO j4 = new JobMainPO();
		j4.setCreatedBy("FangfangXu");
		j4.setName("j4");
		j4.setType(JobType.Delay);
		j4.setExecutorName("4");
		j4.setJobHandlerName("4");
		j4.setEnd(false);// 需要恢复
		j4.setPriority(10);// 高优先
		j4.setQueuedAtInstance(queuedAtInstance);
		jobMainMapper.add(j4);

		JobMainPO j5 = new JobMainPO();
		j5.setCreatedBy("FangfangXu");
		j5.setName("j5");
		j5.setType(JobType.Delay);
		j5.setExecutorName("5");
		j5.setJobHandlerName("5");
		j5.setEnd(false);// 需要恢复
		j5.setPriority(10);// 高优先与j4相同
		j5.setQueuedAtInstance(queuedAtInstance);
		jobMainMapper.add(j5);
		
		/**
		 * 让任务进入待恢复
		 */
		jobFacadeManager.recoveryThatNoQueuedActuallyByQueuedAtInstance(queuedAtInstance);

		List<ExecutableJobBO> jobs = jobFacadeManager.listJobsShouldRecovery(0, 10);
		Assertions.assertThat(jobs).hasSize(3);
		Assertions.assertThat(jobs.get(0).getPriority()).isEqualTo(10);// 因为priority高
		Assertions.assertThat(jobs.get(1).getPriority()).isEqualTo(10);// 因为priority高
		Assertions.assertThat(jobs.get(2).getPriority()).isEqualTo(1);

		jobs = jobFacadeManager.listJobsShouldRecovery(2, 10);
		Assertions.assertThat(jobs).hasSize(1);
		Assertions.assertThat(jobs.get(0).getPriority()).isEqualTo(1);
	}
}

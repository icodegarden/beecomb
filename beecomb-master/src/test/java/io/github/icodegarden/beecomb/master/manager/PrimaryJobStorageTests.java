package io.github.icodegarden.beecomb.master.manager;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.mapper.DelayJobMapper;
import io.github.icodegarden.beecomb.common.db.mapper.JobDetailMapper;
import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.db.mapper.ScheduleJobMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobDetailPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.ScheduleJobPO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.manager.DelayJobStorage;
import io.github.icodegarden.beecomb.master.manager.PrimaryJobStorage;
import io.github.icodegarden.beecomb.master.manager.ScheduleJobStorage;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class PrimaryJobStorageTests {

	@Autowired
	PrimaryJobStorage primaryJobStorage;
	@MockBean
	DelayJobStorage delayJobStorage;
	@MockBean
	ScheduleJobStorage scheduleJobStorage;
	
	@Autowired
	JobMainMapper jobMainMapper;
	@Autowired
	JobDetailMapper jobDetailMapper;
	@Autowired
	DelayJobMapper delayJobMapper;
	@Autowired
	ScheduleJobMapper scheduleJobMapper;

	@Test
	void create() {
		CreateJobDTO delayJobDTO = new CreateJobDTO();
		delayJobDTO.setName("myjob1");
		delayJobDTO.setType(JobType.Delay);
		primaryJobStorage.create(delayJobDTO);

		verify(delayJobStorage, times(1)).create(delayJobDTO);
		verify(scheduleJobStorage, times(0)).create(delayJobDTO);

		CreateJobDTO scheduleJobDTO = new CreateJobDTO();
		scheduleJobDTO.setName("myjob2");
		scheduleJobDTO.setType(JobType.Schedule);
		primaryJobStorage.create(scheduleJobDTO);

		verify(delayJobStorage, times(0)).create(scheduleJobDTO);
		verify(scheduleJobStorage, times(1)).create(scheduleJobDTO);
	}

	@Test
	void hasNoQueuedActually() {
		LocalDateTime time = LocalDateTime.now().minusSeconds(60);
		boolean b = primaryJobStorage.hasNoQueuedActually(time);
		Assertions.assertThat(b).isFalse();
		
		JobMainPO j1 = new JobMainPO();
		j1.setCreatedBy("FangfangXu");
		j1.setName("j1");
		j1.setType(JobType.Delay);
		j1.setExecutorName("1");
		j1.setJobHandlerName("1");
		j1.setNextTrigAt(time);
		jobMainMapper.add(j1);
		
		b = primaryJobStorage.hasNoQueuedActually(LocalDateTime.now());
		Assertions.assertThat(b).isTrue();
	}
	
	@Test
	void recoveryThatNoQueuedActually() {
		LocalDateTime time = LocalDateTime.now().minusSeconds(60);
		primaryJobStorage.recoveryThatNoQueuedActually(time);
	}
	
	@Test
	void listJobsShouldRecovery () {
		JobMainPO j1 = new JobMainPO();
		j1.setCreatedBy("FangfangXu");
		j1.setName("j1");
		j1.setType(JobType.Delay);
		j1.setExecutorName("1");
		j1.setJobHandlerName("1");
		j1.setEnd(true);//完成的不会查到
		jobMainMapper.add(j1);

		JobMainPO j2 = new JobMainPO();
		j2.setCreatedBy("FangfangXu");
		j2.setName("j2");
		j2.setType(JobType.Schedule);
		j2.setExecutorName("2");
		j2.setJobHandlerName("2");
		j2.setQueued(true);//已队列的不会查到
		jobMainMapper.add(j2);
		
		JobMainPO j3 = new JobMainPO();
		j3.setCreatedBy("FangfangXu");
		j3.setName("j3");
		j3.setType(JobType.Delay);
		j3.setExecutorName("3");
		j3.setJobHandlerName("3");
		j3.setEnd(false);//查到
		j3.setPriority(1);//低优先
		jobMainMapper.add(j3);
		
		JobMainPO j4 = new JobMainPO();
		j4.setCreatedBy("FangfangXu");
		j4.setName("j4");
		j4.setType(JobType.Delay);
		j4.setExecutorName("4");
		j4.setJobHandlerName("4");
		j4.setEnd(false);//查到
		j4.setPriority(10);//高优先
		jobMainMapper.add(j4);
		
		JobMainPO j5 = new JobMainPO();
		j5.setCreatedBy("FangfangXu");
		j5.setName("j5");
		j5.setType(JobType.Delay);
		j5.setExecutorName("5");
		j5.setJobHandlerName("5");
		j5.setEnd(false);//查到
		j5.setPriority(10);//高优先与j4相同
		jobMainMapper.add(j5);
		
		List<ExecutableJobBO> jobs = primaryJobStorage.listJobsShouldRecovery(0,10);
		Assertions.assertThat(jobs).hasSize(3);
		Assertions.assertThat(jobs.get(0).getPriority()).isEqualTo(10);//因为priority高
		Assertions.assertThat(jobs.get(1).getPriority()).isEqualTo(10);//因为priority高
		Assertions.assertThat(jobs.get(2).getPriority()).isEqualTo(1);
		
		jobs = primaryJobStorage.listJobsShouldRecovery(2,10);
		Assertions.assertThat(jobs).hasSize(1);
		Assertions.assertThat(jobs.get(0).getPriority()).isEqualTo(1);
	}
}

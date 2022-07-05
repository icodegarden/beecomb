package io.github.icodegarden.beecomb.worker.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ScheduleBO;
import io.github.icodegarden.beecomb.test.Properties4Test;
import io.github.icodegarden.beecomb.worker.core.JobEngine;
import io.github.icodegarden.beecomb.worker.exception.WorkerException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@Transactional 由于要验证事务的回滚，因此事务不能在这里开启
@SpringBootTest
class JobReceiverTests extends Properties4Test {

	@Autowired
	JobRequestReceiver jobReceiver;
	@SpyBean
	JobEngine jobEngine;
	@Autowired
	JobMainMapper jobMainMapper;
	@Autowired
	JobMainManager jobMainManager;

	ExecutableJobBO job;

	@BeforeEach
	void init() {
		job = new ExecutableJobBO();
		job.setId(1L);
		job.setType(JobType.Schedule);
		ScheduleBO schedule = new ScheduleBO();
		schedule.setSheduleCron("* * * * * *");// 每秒
		job.setSchedule(schedule);
		job.setWeight(3);
	}

	JobMainPO create() {
		JobMainPO jobPO = new JobMainPO();
		jobPO.setCreatedBy("FangfangXu");
		jobPO.setExecuteTimeout(1234);
		jobPO.setName("myjob");
		jobPO.setExecutorName("n");
		jobPO.setJobHandlerName("j");
		jobPO.setQueued(false);
		jobPO.setType(JobType.Schedule);
		jobPO.setWeight(2);
		jobMainMapper.add(jobPO);
		return jobPO;
	}

	@Test
	void receiveFailOn_Overload() {
		// 容量不足-----------------------------------
		assertThatExceptionOfType(WorkerException.class).isThrownBy(() -> jobReceiver.receive(job))
				.withMessage("Exceed Overload");
	}

	@Test
	void receiveFailOn_enQueueFailed() {
		JobMainPO mainPO = create();
		JobMainVO findOne = jobMainManager.findOne(mainPO.getId(), null);
		assertThat(findOne.getQueued()).isFalse();
		assertThat(findOne.getQueuedAt()).isNull();

		job.setId(mainPO.getId());
		job.setWeight(2);

		doThrow(new RuntimeException("doThrow enQueue")).when(jobEngine).enQueue(job);
		assertThatExceptionOfType(WorkerException.class).isThrownBy(() -> jobReceiver.receive(job))
				.withMessage("ex on job en queue").withCauseExactlyInstanceOf(RuntimeException.class);

		// 验证事务的回滚
		findOne = jobMainManager.findOne(mainPO.getId(), null);
		assertThat(findOne.getQueued()).isFalse();
		assertThat(findOne.getQueuedAt()).isNull();

		// 最后手动删除数据
		jobMainMapper.delete(mainPO.getId());
	}

	@Test
	void receive_success() {
		JobMainPO mainPO = create();
		JobMainVO findOne = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(findOne.getQueued()).isFalse();
		assertThat(findOne.getQueuedAt()).isNull();

		job.setId(mainPO.getId());
		job.setWeight(2);

		jobReceiver.receive(job);

		// 验证事务的提交
		findOne = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(findOne.getQueued()).isTrue();
		assertThat(findOne.getQueuedAt()).isNotNull();

		jobMainMapper.delete(mainPO.getId());// 由于本test类没有开启事务，主动删除
	}
}

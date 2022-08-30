package io.github.icodegarden.beecomb.worker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.data.DelayJobDO;
import io.github.icodegarden.beecomb.common.backend.pojo.data.JobMainDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class PrimaryJobServiceTests {

	@Autowired
	PrimaryJobService primaryJobStorage;
	@Autowired
	JobMainMapper jobMainMapper;

	JobMainPO create() {
		JobMainPO jobPO = new JobMainPO();
		jobPO.setCreatedBy("FangfangXu");
		jobPO.setEnd(false);
		jobPO.setExecuteTimeout(1234);
		jobPO.setLastExecuteExecutor("1.1.1.1:8080");
		jobPO.setLastExecuteSuccess(true);
		LocalDateTime lastTrigAt = LocalDateTime.now().minusHours(1);
		jobPO.setLastTrigAt(lastTrigAt);
		jobPO.setName("myjob");
		jobPO.setExecutorName("n");
		jobPO.setJobHandlerName("j");
		jobPO.setPriority(5);
		jobPO.setQueued(false);
		jobPO.setType(JobType.Schedule);
		String uuid = UUID.randomUUID().toString();
		jobPO.setUuid(uuid);
		jobPO.setWeight(1);
		jobPO.setCreatedAt(LocalDateTime.now());
		jobPO.setUpdatedAt(LocalDateTime.now());

		jobMainMapper.add(jobPO);

		return jobPO;
	}

	@Test
	void updateEnQueue() {
		JobMainPO mainPO = create();
		assertThat(mainPO.getQueued()).isFalse();
		assertThat(mainPO.getQueuedAt()).isNull();
		assertThat(mainPO.getQueuedAtInstance()).isNull();

		JobMainDO jobDO = new JobMainDO();
		BeanUtils.copyProperties(mainPO, jobDO);
		DelayJobDO delayJobPO = new DelayJobDO();
		delayJobPO.setDelay(3000L);
		jobDO.setDelayJob(delayJobPO);
		ExecutableJobBO job = jobDO.toExecutableJobBO();

		primaryJobStorage.updateEnQueue(job);
		
		JobMainDO findOne = jobMainMapper.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(findOne.getQueued()).isTrue();
		assertThat(findOne.getQueuedAt()).isNotNull();
		assertThat(findOne.getQueuedAtInstance()).isNotNull();
	}

	@Test
	void updateOnNoQualifiedExecutor() {
		assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> primaryJobStorage.updateOnNoQualifiedExecutor(null)).withMessage("not supported");
	}

	@Test
	void updateOnExecuteSuccess() {
		assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> primaryJobStorage.updateOnExecuteSuccess(null)).withMessage("not supported");
	}

	@Test
	void updateOnExecuteFailed() {
		assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> primaryJobStorage.updateOnExecuteFailed(null)).withMessage("not supported");
	}
}

//package io.github.icodegarden.beecomb.worker.manager;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
//import io.github.icodegarden.beecomb.common.db.pojo.data.JobDO;
//import io.github.icodegarden.beecomb.common.db.pojo.persistence.DelayJobPO;
//import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
//import io.github.icodegarden.beecomb.common.db.pojo.query.JobQuery;
//import io.github.icodegarden.beecomb.common.enums.JobType;
//import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//@Transactional
//@SpringBootTest
//class JobManagerTests {
//
//	@Autowired
//	JobManager jobManager;
//	@Autowired
//	JobMainMapper jobMainMapper;
//
//	JobMainPO create() {
//		JobMainPO jobPO = new JobMainPO();
//		jobPO.setCreatedBy("FangfangXu");
//		jobPO.setEnd(false);
//		jobPO.setExecuteTimeout(1234);
//		jobPO.setLastExecuteExecutor("1.1.1.1:8080");
//		jobPO.setLastExecuteReturns("[{}]");
//		jobPO.setLastExecuteSuccess(true);
//		LocalDateTime lastTrigAt = LocalDateTime.now().minusHours(1);
//		jobPO.setLastTrigAt(lastTrigAt);
//		jobPO.setLastTrigResult("a.b.c");
//		jobPO.setName("myjob");
//		jobPO.setExecutorName("n");
//		jobPO.setJobHandlerName("j");
//		jobPO.setPriority(5);
//		jobPO.setQueued(false);
//		jobPO.setType(JobType.Schedule);
//		String uuid = UUID.randomUUID().toString();
//		jobPO.setUuid(uuid);
//		jobPO.setWeight(1);
//		jobPO.setCreatedAt(LocalDateTime.now());
//
//		jobMainMapper.add(jobPO);
//
//		return jobPO;
//	}
//
//	@Test
//	void updateEnQueue() {
//		JobMainPO mainPO = create();
//		assertThat(mainPO.getQueued()).isFalse();
//		assertThat(mainPO.getQueuedAt()).isNull();
//		assertThat(mainPO.getQueuedAtInstance()).isNull();
//
//		JobDO jobDO = new JobDO();
//		jobDO.setJobMain(mainPO);
//		DelayJobPO delayJobPO = new DelayJobPO();
//		delayJobPO.setDelay(3000);
//		jobDO.setDelayJob(delayJobPO);
//		ExecutableJobBO job = jobDO.toExecutableJobBO();
//
//		jobManager.updateEnQueue(job);
//		JobDO find = jobMainMapper.findOne(mainPO.getId(), JobQuery.With.WITH_MOST);
//		JobMainPO findOne = find.getJobMain();
//		assertThat(findOne.getQueued()).isTrue();
//		assertThat(findOne.getQueuedAt()).isNotNull();
//		assertThat(findOne.getQueuedAtInstance()).isNotNull();
//	}
//}

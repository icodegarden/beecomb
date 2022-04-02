package io.github.icodegarden.beecomb.master.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.mapper.DelayJobMapper;
import io.github.icodegarden.beecomb.common.db.mapper.JobDetailMapper;
import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.db.mapper.ScheduleJobMapper;
import io.github.icodegarden.beecomb.common.db.pojo.data.JobDO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobDetailPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO.Update;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.ScheduleJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobQuery;
import io.github.icodegarden.beecomb.common.enums.JobType;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class JobMainMapperTests {

	@Autowired
	JobMainMapper jobMainMapper;
	@Autowired
	JobDetailMapper jobDetailMapper;
	@Autowired
	DelayJobMapper delayJobMapper;
	@Autowired
	ScheduleJobMapper scheduleJobMapper;

	JobMainPO create() {
		JobMainPO jobPO = new JobMainPO();
		jobPO.setCreatedBy("FangfangXu");
		jobPO.setEnd(false);// end的任务不会更新
		jobPO.setExecuteTimeout(1234);
		jobPO.setLastExecuteExecutor("1.1.1.1:8080");
		jobPO.setLastExecuteReturns("[{}]");
		jobPO.setLastExecuteSuccess(true);
		LocalDateTime lastTrigAt = LocalDateTime.now().minusHours(1);
		jobPO.setLastTrigAt(lastTrigAt);
		jobPO.setLastTrigResult("a.b.c");
		jobPO.setName("myjob");
		jobPO.setPriority(5);
		jobPO.setQueued(true);
		jobPO.setType(JobType.Schedule);
		jobPO.setExecutorName("myExecutorName");
		jobPO.setJobHandlerName("myJobHandlerName");
		String uuid = UUID.randomUUID().toString();
		jobPO.setUuid(uuid);
		jobPO.setWeight(1);
		jobPO.setNextTrigAt(LocalDateTime.now().minusHours(1));

		jobMainMapper.add(jobPO);

		return jobPO;
	}

	@Test
	void findOne() {
		JobMainPO j1 = new JobMainPO();
		j1.setCreatedBy("FangfangXu");
		j1.setName("myjob");
		j1.setUuid("uuid_uuid");
		j1.setType(JobType.Delay);
		j1.setExecutorName("myExecutorName");
		j1.setJobHandlerName("myJobHandlerName");
		j1.setLastTrigAt(LocalDateTime.now().minusHours(1).withNano(0).withSecond(0));
		j1.setNextTrigAt(LocalDateTime.now().plusHours(1).withNano(0).withSecond(0));
		j1.setExecuteTimeout(1234);
		j1.setLastExecuteExecutor("1.1.1.1:8080");
		j1.setLastExecuteReturns("[{}]");
		j1.setLastExecuteSuccess(true);
		j1.setLastTrigResult("a.b.c");
		j1.setWeight(1);
		j1.setPriority(5);
		j1.setQueued(true);
		jobMainMapper.add(j1);

		JobDetailPO jd1 = new JobDetailPO();
		jd1.setJobId(j1.getId());
		jd1.setParams("params1");
		jd1.setDesc("desc1");
		jobDetailMapper.add(jd1);

		DelayJobPO dj1 = new DelayJobPO();
		dj1.setJobId(j1.getId());
		dj1.setDelay(3000);
		delayJobMapper.add(dj1);

		JobQuery.With with = JobQuery.With.builder()
				.jobMain(JobQuery.With.JobMain.builder().createdAt(true).createdBy(true).lastExecuteExecutor(true)
						.lastExecuteReturns(true).lastTrigResult(true).queuedAt(true).queuedAtInstance(true).build())
				.jobDetail(JobQuery.With.JobDetail.builder().params(true).desc(true).build())
				.delayJob(JobQuery.With.DelayJob.builder().build())
				.scheduleJob(JobQuery.With.ScheduleJob.builder().build()).build();

		JobDO job = jobMainMapper.findOne(j1.getId(), with);
		assertThat(job.getJobMain()).isNotNull();
		assertThat(job.getJobDetail()).isNotNull();
		assertThat(job.getDelayJob()).isNotNull();

		JobMainPO findOne = job.getJobMain();

		assertThat(findOne).isNotNull();
		assertThat(findOne.getId()).isEqualTo(j1.getId());
		assertThat(findOne.getCreatedBy()).isEqualTo("FangfangXu");
		assertThat(findOne.getEnd()).isEqualTo(false);
		assertThat(findOne.getExecuteTimeout()).isEqualTo(1234);
		assertThat(findOne.getLastExecuteExecutor()).isEqualTo("1.1.1.1:8080");
		assertThat(findOne.getLastExecuteReturns()).isEqualTo("[{}]");
		assertThat(findOne.getLastExecuteSuccess()).isEqualTo(true);
		assertThat(findOne.getLastTrigAt()).isEqualTo(j1.getLastTrigAt());
		assertThat(findOne.getLastTrigResult()).isEqualTo("a.b.c");
		assertThat(findOne.getName()).isEqualTo("myjob");
		assertThat(findOne.getExecutorName()).isEqualTo("myExecutorName");
		assertThat(findOne.getJobHandlerName()).isEqualTo("myJobHandlerName");
		assertThat(findOne.getPriority()).isEqualTo(5);
		assertThat(findOne.getQueued()).isEqualTo(true);
		assertThat(findOne.getType()).isEqualTo(JobType.Delay);
		assertThat(findOne.getUuid()).isEqualTo(j1.getUuid());
		assertThat(findOne.getWeight()).isEqualTo(1);
		assertThat(findOne.getNextTrigAt()).isEqualTo(j1.getNextTrigAt());
	}

	@Test
	void findAll() {
		JobMainPO j1 = new JobMainPO();
		j1.setCreatedBy("FangfangXu");
		j1.setName("j1");
		j1.setType(JobType.Delay);
		j1.setExecutorName("1");
		j1.setJobHandlerName("1");
		jobMainMapper.add(j1);

		JobDetailPO jd1 = new JobDetailPO();
		jd1.setJobId(j1.getId());
		jd1.setParams("params1");
		jd1.setDesc("desc1");
		jobDetailMapper.add(jd1);

		DelayJobPO dj1 = new DelayJobPO();
		dj1.setJobId(j1.getId());
		dj1.setDelay(3000);
		delayJobMapper.add(dj1);

		JobMainPO j2 = new JobMainPO();
		j2.setCreatedBy("FangfangXu");
		j2.setName("j2");
		j2.setType(JobType.Schedule);
		j2.setExecutorName("2");
		j2.setJobHandlerName("2");
		jobMainMapper.add(j2);

		JobDetailPO jd2 = new JobDetailPO();
		jd2.setJobId(j2.getId());
		jd2.setParams("params2");
		jd2.setDesc("desc2");
		jobDetailMapper.add(jd2);

		ScheduleJobPO sj2 = new ScheduleJobPO();
		sj2.setJobId(j2.getId());
		scheduleJobMapper.add(sj2);

		// 不关联查询-------------------------------------
		JobQuery query = JobQuery.builder().sort("order by a.id asc").limit("limit 0,10").build();
		List<JobDO> list = jobMainMapper.findAll(query);

		assertThat(list).hasSize(2);
		JobDO job = list.get(0);
		System.out.println(job);
		assertThat(job.getJobMain()).isNotNull();
		assertThat(job.getJobDetail()).isNull();
		assertThat(job.getDelayJob()).isNull();
		assertThat(job.getScheduleJob()).isNull();
		job = list.get(1);
		System.out.println(job);
		assertThat(job.getJobMain()).isNotNull();
		assertThat(job.getJobDetail()).isNull();
		assertThat(job.getScheduleJob()).isNull();
		assertThat(job.getDelayJob()).isNull();

		// 关联查询-------------------------------------
		query = JobQuery.builder()
				.with(JobQuery.With.builder()
						.jobDetail(JobQuery.With.JobDetail.builder().params(true).desc(true).build())
						.delayJob(JobQuery.With.DelayJob.builder().build())
						.scheduleJob(JobQuery.With.ScheduleJob.builder().build()).build())
				.sort("order by a.id asc").limit("limit 0,10").build();
		list = jobMainMapper.findAll(query);

		assertThat(list).hasSize(2);
		job = list.get(0);
		System.out.println(job);
		assertThat(job.getJobMain()).isNotNull();
		assertThat(job.getJobDetail()).isNotNull();
		assertThat(job.getDelayJob()).isNotNull();
		assertThat(job.getScheduleJob()).isNull();
		job = list.get(1);
		System.out.println(job);
		assertThat(job.getJobMain()).isNotNull();
		assertThat(job.getJobDetail()).isNotNull();
		assertThat(job.getScheduleJob()).isNotNull();
		assertThat(job.getDelayJob()).isNull();
	}

	@Test
	void update() {
		JobMainPO jobPO = create();
		Long id = jobPO.getId();

		LocalDateTime nextTrigAt = LocalDateTime.now().plusMinutes(50).withNano(0).withSecond(0);
		Update update = JobMainPO.Update.builder().id(id).end(false).executeTimeout(10000)
				.lastExecuteExecutor("3.3.3.3:8888").lastExecuteReturns("123456").lastExecuteSuccess(true)
				.lastTrigAt(LocalDateTime.now()).lastTrigResult("001002").name("newname").priority(5).queued(true)
				.queuedAt(LocalDateTime.now()).weight(5).nextTrigAt(nextTrigAt).build();

		jobMainMapper.update(update);

		JobQuery.With with = JobQuery.With.builder()
				.jobMain(JobQuery.With.JobMain.builder().createdAt(true).createdBy(true).lastExecuteExecutor(true)
						.lastExecuteReturns(true).lastTrigResult(true).queuedAt(true).queuedAtInstance(true).build())
				.jobDetail(JobQuery.With.JobDetail.builder().params(true).desc(true).build())
				.delayJob(JobQuery.With.DelayJob.builder().build())
				.scheduleJob(JobQuery.With.ScheduleJob.builder().build()).build();

		JobDO job = jobMainMapper.findOne(id, with);
		JobMainPO findOne = job.getJobMain();

		assertThat(findOne).isNotNull();
		assertThat(findOne.getId()).isEqualTo(id);
		assertThat(findOne.getEnd()).isEqualTo(false);
		assertThat(findOne.getExecuteTimeout()).isEqualTo(10000);
		assertThat(findOne.getLastExecuteExecutor()).isEqualTo("3.3.3.3:8888");
		assertThat(findOne.getLastExecuteReturns()).isEqualTo("123456");
		assertThat(findOne.getLastExecuteSuccess()).isEqualTo(true);
		assertThat(findOne.getLastTrigResult()).isEqualTo("001002");
		assertThat(findOne.getName()).isEqualTo("newname");
		assertThat(findOne.getPriority()).isEqualTo(5);
		assertThat(findOne.getQueued()).isEqualTo(true);
		assertThat(findOne.getWeight()).isEqualTo(5);
		assertThat(findOne.getNextTrigAt()).isEqualTo(nextTrigAt);

		// null的更新--------------------------------------
		update = JobMainPO.Update.builder().id(id).nextTrigAtNull(true).build();
		jobMainMapper.update(update);

		job = jobMainMapper.findOne(id, with);
		findOne = job.getJobMain();
		assertThat(findOne.getNextTrigAt()).isNull();
	}

	@Test
	void delete() {
		JobMainPO jobPO = create();
		Long id = jobPO.getId();

		jobMainMapper.delete(id);

		JobDO findOne = jobMainMapper.findOne(id, null);
		assertThat(findOne).isNull();
	}

	/**
	 * 对nextTrigAt超时的数据更新为queued=false，nextTrigAt=null
	 */
	@Test
	void updateToNoQueued() {
		JobMainPO job2 = new JobMainPO();
		job2.setName("job2");
		job2.setType(JobType.Schedule);
		job2.setExecutorName("n1");
		job2.setJobHandlerName("j1");
		job2.setQueued(true);
		job2.setNextTrigAt(LocalDateTime.now().minusSeconds(30));
		jobMainMapper.add(job2);

		// 时间参数不满足，不会更新-------------------------
		jobMainMapper.updateToNoQueued(LocalDateTime.now().minusSeconds(60));

		JobDO job = jobMainMapper.findOne(job2.getId(), null);
		JobMainPO findJob2 = job.getJobMain();
		Assertions.assertThat(findJob2.getQueued()).isTrue();
		Assertions.assertThat(findJob2.getNextTrigAt()).isNotNull();

		// 时间参数满足，更新成功---------------------------------------
		jobMainMapper.updateToNoQueued(LocalDateTime.now().minusSeconds(29));
		job = jobMainMapper.findOne(job2.getId(), null);
		findJob2 = job.getJobMain();
		Assertions.assertThat(findJob2.getQueued()).isFalse();
		Assertions.assertThat(findJob2.getNextTrigAt()).isNull();
	}
}

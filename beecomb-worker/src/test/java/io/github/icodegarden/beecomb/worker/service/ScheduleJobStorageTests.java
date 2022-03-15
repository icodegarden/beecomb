package io.github.icodegarden.beecomb.worker.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.db.mapper.ScheduleJobMapper;
import io.github.icodegarden.beecomb.common.db.pojo.data.JobDO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.ScheduleJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobWith;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.worker.service.JobStorage.UpdateOnExecuteFailed;
import io.github.icodegarden.beecomb.worker.service.JobStorage.UpdateOnExecuteSuccess;
import io.github.icodegarden.beecomb.worker.service.JobStorage.UpdateOnNoQualifiedExecutor;
import io.github.icodegarden.commons.exchange.exception.AllInstanceFailedExchangeException;
import io.github.icodegarden.commons.exchange.exception.NoQualifiedInstanceExchangeException;
import io.github.icodegarden.commons.lang.result.Result1;
import io.github.icodegarden.commons.lang.result.Result2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class ScheduleJobStorageTests {

	@Autowired
	ScheduleJobStorage scheduleJobStorage;
	@Autowired
	JobMainMapper jobMainMapper;
	@Autowired
	ScheduleJobMapper scheduleJobMapper;

	JobMainPO mainPO;

	JobMainPO create() {
		mainPO = new JobMainPO();
		mainPO.setCreatedBy("FangfangXu");
		mainPO.setName("myjob");
		mainPO.setExecutorName("n");
		mainPO.setJobHandlerName("j");
		mainPO.setPriority(5);
		mainPO.setType(JobType.Schedule);
		mainPO.setWeight(1);
		mainPO.setQueued(true);
		jobMainMapper.add(mainPO);
		return mainPO;
	}

	@BeforeEach
	void init() {
		// 初始化数据
		JobMainPO mainPO = create();

		ScheduleJobPO scheduleJobPO = new ScheduleJobPO();
		scheduleJobPO.setJobId(mainPO.getId());
		scheduleJobPO.setScheduleFixRate(3000);
		scheduleJobMapper.add(scheduleJobPO);
	}

	@Test
	void updateOnNoCandidateExecutor() {
		for (int i = 1; i < 10; i++) {
			LocalDateTime now = LocalDateTime.now().withNano(0);

			UpdateOnNoQualifiedExecutor update = UpdateOnNoQualifiedExecutor.builder().jobId(mainPO.getId())
					.lastTrigAt(now)
					.noQualifiedInstanceExchangeException(new NoQualifiedInstanceExchangeException(null))
					.nextTrigAt(LocalDateTime.now()).build();
			Result2<Boolean, RuntimeException> result2 = scheduleJobStorage.updateOnNoQualifiedExecutor(update);

			assertThat(result2.isSuccess()).isEqualTo(true);
			assertThat(result2.getT1()).isEqualTo(false);// 始终是false

			JobDO find = jobMainMapper.findOne(mainPO.getId(), JobWith.WITH_MOST);
			JobMainPO main = find.getJobMain();
			assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
			assertThat(main.getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// 最近触发的结果描述是由于NoQualified
			assertThat(main.getEnd()).isEqualTo(false);// 始终不会结束
			assertThat(main.getQueued()).isEqualTo(true);// 始终是queued状态

			ScheduleJobPO scheduleJob = scheduleJobMapper.findOne(mainPO.getId());
			assertThat(scheduleJob.getScheduledTimes()).isEqualTo(i);// 递增
		}
	}

	@Test
	void updateOnExecuteSuccess() {
		for (int i = 1; i < 10; i++) {
			LocalDateTime now = LocalDateTime.now().withNano(0);

			UpdateOnExecuteSuccess update = UpdateOnExecuteSuccess.builder().jobId(mainPO.getId()).lastTrigAt(now)
					.executorIp("1.1.1.1").executorPort(10000 + i).lastExecuteReturns(i + "")
					.nextTrigAt(LocalDateTime.now()).build();

			Result1<RuntimeException> result1 = scheduleJobStorage.updateOnExecuteSuccess(update);
			assertThat(result1.isSuccess()).isEqualTo(true);

			JobDO find = jobMainMapper.findOne(mainPO.getId(), JobWith.WITH_MOST);
			JobMainPO main = find.getJobMain();
			assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
			assertThat(main.getLastTrigResult()).isEqualTo("Success");// 最近触发的结果描述是Success
			assertThat(main.getEnd()).isEqualTo(false);// 不会结束
			assertThat(main.getQueued()).isEqualTo(true);// 始终是queued状态
			assertThat(main.getLastExecuteExecutor()).isEqualTo("1.1.1.1:" + (10000 + i));//
			assertThat(main.getLastExecuteReturns()).isEqualTo(i + "");//
			assertThat(main.getLastExecuteSuccess()).isEqualTo(true);//

			ScheduleJobPO scheduleJob = scheduleJobMapper.findOne(mainPO.getId());
			assertThat(scheduleJob.getScheduledTimes()).isEqualTo(i);// 递增
		}
	}

	@Test
	void updateOnExecuteSuccess_end() {
		LocalDateTime now = LocalDateTime.now().withNano(0);
		for (int i = 1; i < 10; i++) {
			UpdateOnExecuteSuccess update = UpdateOnExecuteSuccess.builder().jobId(mainPO.getId()).lastTrigAt(now)
					.executorIp("1.1.1.1").executorPort(10000 + i).lastExecuteReturns(i + "").end(true)
					.nextTrigAt(LocalDateTime.now()).build();

			Result1<RuntimeException> result1 = scheduleJobStorage.updateOnExecuteSuccess(update);
			assertThat(result1.isSuccess()).isEqualTo(true);

			JobDO find = jobMainMapper.findOne(mainPO.getId(), JobWith.WITH_MOST);
			JobMainPO main = find.getJobMain();
			assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
			assertThat(main.getLastTrigResult()).isEqualTo("Success");// 最近触发的结果描述是Success
			assertThat(main.getEnd()).isEqualTo(true);// 结束
			assertThat(main.getQueued()).isEqualTo(false);// 不是queued状态
			assertThat(main.getLastExecuteExecutor()).isEqualTo("1.1.1.1:" + (10000 + 1));// 不变
			assertThat(main.getLastExecuteReturns()).isEqualTo(1 + "");// 不变
			assertThat(main.getLastExecuteSuccess()).isEqualTo(true);//

			ScheduleJobPO scheduleJob = scheduleJobMapper.findOne(mainPO.getId());
			assertThat(scheduleJob.getScheduledTimes()).isEqualTo(1);// 不变
		}
	}

	@Test
	void updateOnExecuteFailed() {
		for (int i = 1; i < 10; i++) {
			LocalDateTime now = LocalDateTime.now().withNano(0);

			UpdateOnExecuteFailed update = UpdateOnExecuteFailed.builder().jobId(mainPO.getId()).lastTrigAt(now)
					.exception(new AllInstanceFailedExchangeException(null, null)).nextTrigAt(LocalDateTime.now())
					.build();
			Result2<Boolean, RuntimeException> result2 = scheduleJobStorage.updateOnExecuteFailed(update);

			assertThat(result2.isSuccess()).isEqualTo(true);
			assertThat(result2.getT1()).isEqualTo(false);// 始终不会到失败阈值

			JobDO find = jobMainMapper.findOne(mainPO.getId(), JobWith.WITH_MOST);
			JobMainPO main = find.getJobMain();
			assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
			assertThat(main.getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// 最近触发的结果描述是由于All
																										// Instance
																										// Failed
			assertThat(main.getEnd()).isEqualTo(false);// 还没结束
			assertThat(main.getQueued()).isEqualTo(true);// 始终是queued状态

			ScheduleJobPO scheduleJob = scheduleJobMapper.findOne(mainPO.getId());
			assertThat(scheduleJob.getScheduledTimes()).isEqualTo(i);// 递增
		}
	}
}

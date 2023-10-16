package io.github.icodegarden.beecomb.worker.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.JobDetailManager;
import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.manager.ScheduleJobManager;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobDetailDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobMainDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateScheduleJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobMainEnQueueDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.ScheduleJobVO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteFailedDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteSuccessDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnNoQualifiedExecutorDTO;
import io.github.icodegarden.nutrient.exchange.exception.AllInstanceFailedExchangeException;
import io.github.icodegarden.nutrient.exchange.exception.NoQualifiedInstanceExchangeException;
import io.github.icodegarden.nutrient.lang.result.Result1;
import io.github.icodegarden.nutrient.lang.result.Result2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class ScheduleJobServiceTests {

	@Autowired
	private JobMainManager jobMainManager;
	@Autowired
	private JobDetailManager jobDetailManager;
	@Autowired
	private ScheduleJobManager scheduleJobManager;
	@Autowired
	private ScheduleJobService scheduleJobService;

	CreateJobMainDTO createJobMainDTO;

	void createJobMain() {
		CreateJobMainDTO dto = new CreateJobMainDTO();
		dto.setName("myjob");
		dto.setUuid(UUID.randomUUID().toString());
		dto.setType(JobType.Schedule);
		dto.setExecutorName("n1");
		dto.setJobHandlerName("j1");
		dto.setCreatedBy("beecomb");

		jobMainManager.create(dto);

		/**
		 * 需要enqueue的状态
		 */
		UpdateJobMainEnQueueDTO updateJobMainEnQueueDTO = UpdateJobMainEnQueueDTO.builder().id(dto.getId())
				.nextTrigAt(LocalDateTime.now()).queuedAtInstance("1.1.1.1").build();
		jobMainManager.updateEnQueue(updateJobMainEnQueueDTO);

		CreateJobDetailDTO createJobDetailDTO = new CreateJobDetailDTO();
		createJobDetailDTO.setJobId(dto.getId());
		createJobDetailDTO.setParams("params");
		createJobDetailDTO.setDesc("desc");
		jobDetailManager.create(createJobDetailDTO);
		
		createJobMainDTO = dto;
	}

	@BeforeEach
	void init() {
		createJobMain();

		CreateScheduleJobDTO dto = new CreateScheduleJobDTO();
		dto.setScheduleFixRate(3000L);
		dto.setJobId(createJobMainDTO.getId());

		scheduleJobManager.create(dto);
	}

	@Test
	void updateOnNoCandidateExecutor() {
		for (int i = 1; i < 10; i++) {
			LocalDateTime now = LocalDateTime.now().withNano(0);

			UpdateOnNoQualifiedExecutorDTO update = UpdateOnNoQualifiedExecutorDTO.builder().jobId(createJobMainDTO.getId())
					.lastTrigAt(now)
					.noQualifiedInstanceExchangeException(new NoQualifiedInstanceExchangeException(null))
					.nextTrigAt(LocalDateTime.now()).build();
			Result2<Boolean, RuntimeException> result2 = scheduleJobService.updateOnNoQualifiedExecutor(update);

			assertThat(result2.isSuccess()).isEqualTo(true);
			assertThat(result2.getT1()).isEqualTo(false);// 始终是false

			JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
			assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
			assertThat(main.getJobDetail().getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// 最近触发的结果描述是由于NoQualified
			assertThat(main.getEnd()).isEqualTo(false);// 始终不会结束
			assertThat(main.getQueued()).isEqualTo(true);// 始终是queued状态

			ScheduleJobVO scheduleJob = scheduleJobManager.findOne(createJobMainDTO.getId(), null);
			assertThat(scheduleJob.getScheduledTimes()).isEqualTo(i);// 递增
		}
	}

	@Test
	void updateOnExecuteSuccess() {
		for (int i = 1; i < 10; i++) {
			LocalDateTime now = LocalDateTime.now().withNano(0);

			UpdateOnExecuteSuccessDTO update = UpdateOnExecuteSuccessDTO.builder().jobId(createJobMainDTO.getId())
					.lastTrigAt(now).executorIp("1.1.1.1").executorPort(10000 + i).lastExecuteReturns(i + "")
					.nextTrigAt(LocalDateTime.now()).build();

			Result1<RuntimeException> result1 = scheduleJobService.updateOnExecuteSuccess(update);
			assertThat(result1.isSuccess()).isEqualTo(true);

			JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
			assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
			assertThat(main.getJobDetail().getLastTrigResult()).isEqualTo("Success");// 最近触发的结果描述是Success
			assertThat(main.getEnd()).isEqualTo(false);// 不会结束
			assertThat(main.getQueued()).isEqualTo(true);// 始终是queued状态
			assertThat(main.getLastExecuteExecutor()).isEqualTo("1.1.1.1:" + (10000 + i));//
			assertThat(main.getJobDetail().getLastExecuteReturns()).isEqualTo(i + "");//
			assertThat(main.getLastExecuteSuccess()).isEqualTo(true);//

			ScheduleJobVO scheduleJob = scheduleJobManager.findOne(createJobMainDTO.getId(), null);
			assertThat(scheduleJob.getScheduledTimes()).isEqualTo(i);// 递增
		}
	}

	@Test
	void updateOnExecuteSuccess_end() {
		LocalDateTime now = LocalDateTime.now().withNano(0);
		for (int i = 1; i < 10; i++) {
			UpdateOnExecuteSuccessDTO update = UpdateOnExecuteSuccessDTO.builder().jobId(createJobMainDTO.getId())
					.lastTrigAt(now).executorIp("1.1.1.1").executorPort(10000 + i).lastExecuteReturns(i + "").end(true)
					.nextTrigAt(LocalDateTime.now()).build();

			Result1<RuntimeException> result1 = scheduleJobService.updateOnExecuteSuccess(update);
			assertThat(result1.isSuccess()).isEqualTo(true);

			JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
			assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
			assertThat(main.getJobDetail().getLastTrigResult()).isEqualTo("Success");// 最近触发的结果描述是Success
			assertThat(main.getEnd()).isEqualTo(true);// 结束
			assertThat(main.getQueued()).isEqualTo(false);// 不是queued状态
		}
	}

	@Test
	void updateOnExecuteFailed() {
		for (int i = 1; i < 10; i++) {
			LocalDateTime now = LocalDateTime.now().withNano(0);

			UpdateOnExecuteFailedDTO update = UpdateOnExecuteFailedDTO.builder().jobId(createJobMainDTO.getId())
					.lastTrigAt(now).exception(new AllInstanceFailedExchangeException(null, null))
					.nextTrigAt(LocalDateTime.now()).build();
			Result2<Boolean, RuntimeException> result2 = scheduleJobService.updateOnExecuteFailed(update);

			assertThat(result2.isSuccess()).isEqualTo(true);
			assertThat(result2.getT1()).isEqualTo(false);// 始终不会到失败阈值

			JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
			assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
			assertThat(main.getJobDetail().getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// 最近触发的结果描述是由于All
																										// Instance
																										// Failed
			assertThat(main.getEnd()).isEqualTo(false);// 还没结束
			assertThat(main.getQueued()).isEqualTo(true);// 始终是queued状态

			ScheduleJobVO scheduleJob = scheduleJobManager.findOne(createJobMainDTO.getId(), null);
			assertThat(scheduleJob.getScheduledTimes()).isEqualTo(i);// 递增
		}
	}
}

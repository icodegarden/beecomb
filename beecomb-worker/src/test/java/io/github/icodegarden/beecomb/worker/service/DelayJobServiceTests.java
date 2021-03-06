package io.github.icodegarden.beecomb.worker.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.DelayJobManager;
import io.github.icodegarden.beecomb.common.backend.manager.JobDetailManager;
import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobDetailDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobMainDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.DelayJobVO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteFailedDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteSuccessDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnNoQualifiedExecutorDTO;
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
class DelayJobServiceTests {

	@Autowired
	private JobMainManager jobMainManager;
	@Autowired
	private JobDetailManager jobDetailManager;
	@Autowired
	private DelayJobManager delayJobManager;
	@Autowired
	private DelayJobService delayJobService;
	
	CreateJobMainDTO createJobMainDTO;

	void createJobMain() {
		CreateJobMainDTO dto = new CreateJobMainDTO();
		dto.setName("myjob");
		dto.setType(JobType.Delay);
		dto.setPriority(5);
		dto.setWeight(1);
		dto.setExecutorName("n1");
		dto.setJobHandlerName("j1");
		dto.setCreatedBy("beecomb");

		jobMainManager.create(dto);
		
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

		CreateDelayJobDTO delay = new CreateDelayJobDTO();
		delay.setJobId(createJobMainDTO.getId());
		delay.setDelay(3000L);
		delay.setRetryOnExecuteFailed(2);//???????????????
		delay.setRetryOnNoQualified(2);//???????????????

		delayJobManager.create(delay);
	}

	@Test
	void updateOnNoCandidateExecutor() {
		// ------------------------------------???1?????????
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnNoQualifiedExecutorDTO update = UpdateOnNoQualifiedExecutorDTO.builder().jobId(createJobMainDTO.getId()).lastTrigAt(now)
				.noQualifiedInstanceExchangeException(new NoQualifiedInstanceExchangeException(null)).build();
		Result2<Boolean, RuntimeException> result2 = delayJobService.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// ??????????????????

		JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// ?????????????????????
		assertThat(main.getJobDetail().getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// ????????????????????????????????????NoQualified
		assertThat(main.getEnd()).isEqualTo(false);// ????????????

		DelayJobVO delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(0);// ???1???????????????0

		// ------------------------------------???2?????????
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobService.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// ??????????????????

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// ?????????????????????
		assertThat(main.getJobDetail().getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// ????????????????????????????????????NoQualified
		assertThat(main.getEnd()).isEqualTo(false);// ????????????

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(1);// ???2???????????????1

		// ------------------------------------???3?????????
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobService.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// ???????????????

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// ?????????????????????
		assertThat(main.getJobDetail().getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// ????????????????????????????????????NoQualified
		assertThat(main.getEnd()).isEqualTo(true);// ??????
		assertThat(main.getQueued()).isEqualTo(false);// ??????????????????Queued??????

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(2);// ???3???????????????2

		// ------------------------------------??????????????????true??????????????????
		LocalDateTime now2 = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now2);
		result2 = delayJobService.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// ???????????????

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// ?????????????????????
		assertThat(main.getJobDetail().getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// ????????????????????????????????????NoQualified
		assertThat(main.getEnd()).isEqualTo(true);// ??????

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(2);// ??????2
	}

	@Test
	void updateOnExecuteSuccess() {
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnExecuteSuccessDTO update = UpdateOnExecuteSuccessDTO.builder().jobId(createJobMainDTO.getId()).lastTrigAt(now)
				.executorIp("1.1.1.1").executorPort(10001).lastExecuteReturns("[{}]").build();
		Result1<RuntimeException> result1 = delayJobService.updateOnExecuteSuccess(update);
		assertThat(result1.isSuccess()).isEqualTo(true);

		JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);

		assertThat(main.getLastTrigAt()).isEqualTo(now);// ?????????????????????
		assertThat(main.getJobDetail().getLastTrigResult()).isEqualTo("Success");// ??????????????????????????????Success
		assertThat(main.getEnd()).isEqualTo(true);// ??????
		assertThat(main.getLastExecuteExecutor()).isEqualTo("1.1.1.1:10001");//
		assertThat(main.getJobDetail().getLastExecuteReturns()).isEqualTo("[{}]");//
		assertThat(main.getLastExecuteSuccess()).isEqualTo(true);//
		assertThat(main.getQueued()).isEqualTo(false);// ??????????????????Queued??????

	}

	@Test
	void updateOnExecuteFailed_ExchangeException() {
		// ------------------------------------???1?????????
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnExecuteFailedDTO update = UpdateOnExecuteFailedDTO.builder().jobId(createJobMainDTO.getId()).lastTrigAt(now)
				.exception(new AllInstanceFailedExchangeException(null, null)).build();
		Result2<Boolean, RuntimeException> result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// ??????????????????

		JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// ?????????????????????
		assertThat(main.getJobDetail().getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// ????????????????????????????????????All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(false);// ????????????

		DelayJobPO delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(0);// ???1???????????????0

		// ------------------------------------???2?????????
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// ??????????????????

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// ?????????????????????
		assertThat(main.getJobDetail().getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// ????????????????????????????????????All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(false);// ????????????

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(1);// ???2???????????????1

		// ------------------------------------???3?????????
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// ???????????????

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// ?????????????????????
		assertThat(main.getJobDetail().getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// ????????????????????????????????????All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(true);// ??????
		assertThat(main.getQueued()).isEqualTo(false);// ??????????????????Queued??????

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// ???3???????????????2

		// ------------------------------------??????????????????true??????????????????
		LocalDateTime now2 = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now2);
		result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// ???????????????

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// ?????????????????????
		assertThat(main.getJobDetail().getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// ????????????????????????????????????All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(true);// ??????

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// ??????2
	}

	@Test
	void updateOnExecuteFailed_Exception() {
		// ------------------------------------???1?????????
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnExecuteFailedDTO update = UpdateOnExecuteFailedDTO.builder().jobId(createJobMainDTO.getId()).lastTrigAt(now)
				.exception(new Exception("ex")).build();
		Result2<Boolean, RuntimeException> result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// ???????????????

		JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// ?????????????????????
		assertThat(main.getJobDetail().getLastTrigResult()).isEqualTo("ex");// ?????????????????????????????????????????????
		assertThat(main.getEnd()).isEqualTo(true);// ??????
		assertThat(main.getQueued()).isEqualTo(false);// ??????????????????Queued??????

		DelayJobPO delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// ????????????2

		// ------------------------------------??????????????????true??????????????????
		LocalDateTime now2 = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now2);
		result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// ???????????????

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// ?????????????????????
		assertThat(main.getJobDetail().getLastTrigResult()).isEqualTo("ex");// ?????????????????????????????????????????????
		assertThat(main.getEnd()).isEqualTo(true);// ??????

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// ??????2
	}
}

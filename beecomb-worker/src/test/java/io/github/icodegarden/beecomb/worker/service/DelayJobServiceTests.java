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
		delay.setRetryOnExecuteFailed(2);//关系到测试
		delay.setRetryOnNoQualified(2);//关系到测试

		delayJobManager.create(delay);
	}

	@Test
	void updateOnNoCandidateExecutor() {
		// ------------------------------------第1次触发
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnNoQualifiedExecutorDTO update = UpdateOnNoQualifiedExecutorDTO.builder().jobId(createJobMainDTO.getId()).lastTrigAt(now)
				.noQualifiedInstanceExchangeException(new NoQualifiedInstanceExchangeException(null)).build();
		Result2<Boolean, RuntimeException> result2 = delayJobService.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// 没到失败阈值

		JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getJobDetail().getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// 最近触发的结果描述是由于NoQualified
		assertThat(main.getEnd()).isEqualTo(false);// 还没结束

		DelayJobVO delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(0);// 第1次触发后是0

		// ------------------------------------第2次触发
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobService.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// 没到失败阈值

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getJobDetail().getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// 最近触发的结果描述是由于NoQualified
		assertThat(main.getEnd()).isEqualTo(false);// 还没结束

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(1);// 第2次触发后是1

		// ------------------------------------第3次触发
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobService.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 到失败阈值

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getJobDetail().getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// 最近触发的结果描述是由于NoQualified
		assertThat(main.getEnd()).isEqualTo(true);// 结束
		assertThat(main.getQueued()).isEqualTo(false);// 结束后的不是Queued状态

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(2);// 第3次触发后是2

		// ------------------------------------后续触发返回true，但数据不变
		LocalDateTime now2 = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now2);
		result2 = delayJobService.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 到失败阈值

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getJobDetail().getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// 最近触发的结果描述是由于NoQualified
		assertThat(main.getEnd()).isEqualTo(true);// 结束

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(2);// 还是2
	}

	@Test
	void updateOnExecuteSuccess() {
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnExecuteSuccessDTO update = UpdateOnExecuteSuccessDTO.builder().jobId(createJobMainDTO.getId()).lastTrigAt(now)
				.executorIp("1.1.1.1").executorPort(10001).lastExecuteReturns("[{}]").build();
		Result1<RuntimeException> result1 = delayJobService.updateOnExecuteSuccess(update);
		assertThat(result1.isSuccess()).isEqualTo(true);

		JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);

		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getJobDetail().getLastTrigResult()).isEqualTo("Success");// 最近触发的结果描述是Success
		assertThat(main.getEnd()).isEqualTo(true);// 结束
		assertThat(main.getLastExecuteExecutor()).isEqualTo("1.1.1.1:10001");//
		assertThat(main.getJobDetail().getLastExecuteReturns()).isEqualTo("[{}]");//
		assertThat(main.getLastExecuteSuccess()).isEqualTo(true);//
		assertThat(main.getQueued()).isEqualTo(false);// 结束后的不是Queued状态

	}

	@Test
	void updateOnExecuteFailed_ExchangeException() {
		// ------------------------------------第1次触发
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnExecuteFailedDTO update = UpdateOnExecuteFailedDTO.builder().jobId(createJobMainDTO.getId()).lastTrigAt(now)
				.exception(new AllInstanceFailedExchangeException(null, null)).build();
		Result2<Boolean, RuntimeException> result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// 没到失败阈值

		JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getJobDetail().getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// 最近触发的结果描述是由于All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(false);// 还没结束

		DelayJobPO delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(0);// 第1次触发后是0

		// ------------------------------------第2次触发
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// 没到失败阈值

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getJobDetail().getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// 最近触发的结果描述是由于All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(false);// 还没结束

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(1);// 第2次触发后是1

		// ------------------------------------第3次触发
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 到失败阈值

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getJobDetail().getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// 最近触发的结果描述是由于All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(true);// 结束
		assertThat(main.getQueued()).isEqualTo(false);// 结束后的不是Queued状态

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// 第3次触发后是2

		// ------------------------------------后续触发返回true，但数据不变
		LocalDateTime now2 = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now2);
		result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 到失败阈值

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getJobDetail().getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// 最近触发的结果描述是由于All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(true);// 结束

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// 还是2
	}

	@Test
	void updateOnExecuteFailed_Exception() {
		// ------------------------------------第1次触发
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnExecuteFailedDTO update = UpdateOnExecuteFailedDTO.builder().jobId(createJobMainDTO.getId()).lastTrigAt(now)
				.exception(new Exception("ex")).build();
		Result2<Boolean, RuntimeException> result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 直接到阈值

		JobMainVO main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getJobDetail().getLastTrigResult()).isEqualTo("ex");// 最近触发的结果描述是对应的异常
		assertThat(main.getEnd()).isEqualTo(true);// 结束
		assertThat(main.getQueued()).isEqualTo(false);// 结束后的不是Queued状态

		DelayJobPO delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// 直接等于2

		// ------------------------------------后续触发返回true，但数据不变
		LocalDateTime now2 = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now2);
		result2 = delayJobService.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 到失败阈值

		main = jobMainManager.findOne(createJobMainDTO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getJobDetail().getLastTrigResult()).isEqualTo("ex");// 最近触发的结果描述是对应的异常
		assertThat(main.getEnd()).isEqualTo(true);// 结束

		delayJob = delayJobManager.findOne(createJobMainDTO.getId(), null);
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// 还是2
	}
}

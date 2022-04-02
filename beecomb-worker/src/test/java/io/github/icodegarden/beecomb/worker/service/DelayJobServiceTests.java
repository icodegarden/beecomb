package io.github.icodegarden.beecomb.worker.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.db.mapper.DelayJobMapper;
import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.db.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.worker.service.JobService.UpdateOnExecuteFailed;
import io.github.icodegarden.beecomb.worker.service.JobService.UpdateOnExecuteSuccess;
import io.github.icodegarden.beecomb.worker.service.JobService.UpdateOnNoQualifiedExecutor;
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
	DelayJobService delayJobStorage;
	@Autowired
	JobMainManager jobMainManager;
	@Autowired
	JobMainMapper jobMainMapper;
	@Autowired
	DelayJobMapper delayJobMapper;

	JobMainPO mainPO;

	JobMainPO create() {
		mainPO = new JobMainPO();
		mainPO.setCreatedBy("FangfangXu");
		mainPO.setName("myjob");
		mainPO.setPriority(5);
		mainPO.setType(JobType.Delay);
		mainPO.setWeight(1);
		mainPO.setExecutorName("n");
		mainPO.setJobHandlerName("j");
		mainPO.setQueued(true);
		jobMainMapper.add(mainPO);// TODO 不要使用mapper
		return mainPO;
	}

	@BeforeEach
	void init() {
		// 初始化数据
		JobMainPO mainPO = create();
		DelayJobPO delayJobPO = new DelayJobPO();
		delayJobPO.setJobId(mainPO.getId());
		delayJobPO.setDelay(3000);
		delayJobPO.setRetryOnNoQualified(2);
		delayJobPO.setRetryOnExecuteFailed(2);
		delayJobMapper.add(delayJobPO);
	}

	@Test
	void updateOnNoCandidateExecutor() {
		// ------------------------------------第1次触发
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnNoQualifiedExecutor update = UpdateOnNoQualifiedExecutor.builder().jobId(mainPO.getId()).lastTrigAt(now)
				.noQualifiedInstanceExchangeException(new NoQualifiedInstanceExchangeException(null)).build();
		Result2<Boolean, RuntimeException> result2 = delayJobStorage.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// 没到失败阈值

		JobMainVO main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// 最近触发的结果描述是由于NoQualified
		assertThat(main.getEnd()).isEqualTo(false);// 还没结束

		DelayJobPO delayJob = delayJobMapper.findOne(mainPO.getId());
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(0);// 第1次触发后是0

		// ------------------------------------第2次触发
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobStorage.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// 没到失败阈值

		main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// 最近触发的结果描述是由于NoQualified
		assertThat(main.getEnd()).isEqualTo(false);// 还没结束

		delayJob = delayJobMapper.findOne(mainPO.getId());
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(1);// 第2次触发后是1

		// ------------------------------------第3次触发
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobStorage.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 到失败阈值

		main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// 最近触发的结果描述是由于NoQualified
		assertThat(main.getEnd()).isEqualTo(true);// 结束
		assertThat(main.getQueued()).isEqualTo(false);// 结束后的不是Queued状态

		delayJob = delayJobMapper.findOne(mainPO.getId());
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(2);// 第3次触发后是2

		// ------------------------------------后续触发返回true，但数据不变
		LocalDateTime now2 = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now2);
		result2 = delayJobStorage.updateOnNoQualifiedExecutor(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 到失败阈值

		main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getLastTrigResult()).contains(NoQualifiedInstanceExchangeException.MESSAGE);// 最近触发的结果描述是由于NoQualified
		assertThat(main.getEnd()).isEqualTo(true);// 结束

		delayJob = delayJobMapper.findOne(mainPO.getId());
		assertThat(delayJob.getRetriedTimesOnNoQualified()).isEqualTo(2);// 还是2
	}

	@Test
	void updateOnExecuteSuccess() {
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnExecuteSuccess update = UpdateOnExecuteSuccess.builder().jobId(mainPO.getId()).lastTrigAt(now)
				.executorIp("1.1.1.1").executorPort(10001).lastExecuteReturns("[{}]").build();
		Result1<RuntimeException> result1 = delayJobStorage.updateOnExecuteSuccess(update);
		assertThat(result1.isSuccess()).isEqualTo(true);

		JobMainVO main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);

		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getLastTrigResult()).isEqualTo("Success");// 最近触发的结果描述是Success
		assertThat(main.getEnd()).isEqualTo(true);// 结束
		assertThat(main.getLastExecuteExecutor()).isEqualTo("1.1.1.1:10001");//
		assertThat(main.getLastExecuteReturns()).isEqualTo("[{}]");//
		assertThat(main.getLastExecuteSuccess()).isEqualTo(true);//
		assertThat(main.getQueued()).isEqualTo(false);// 结束后的不是Queued状态

		// ------------------------------------后续触发返回true，但数据不变
		update = UpdateOnExecuteSuccess.builder().jobId(mainPO.getId()).lastTrigAt(LocalDateTime.now().minusHours(1))
				.executorIp("1.1.1.2").executorPort(10002).lastExecuteReturns("[{...}]").build();
		result1 = delayJobStorage.updateOnExecuteSuccess(update);
		assertThat(result1.isSuccess()).isEqualTo(true);

		main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);
		assertThat(main.getLastTrigResult()).isEqualTo("Success");
		assertThat(main.getEnd()).isEqualTo(true);
		assertThat(main.getLastExecuteExecutor()).isEqualTo("1.1.1.1:10001");//
		assertThat(main.getLastExecuteReturns()).isEqualTo("[{}]");//
		assertThat(main.getLastExecuteSuccess()).isEqualTo(true);//
	}

	@Test
	void updateOnExecuteFailed_ExchangeException() {
		// ------------------------------------第1次触发
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnExecuteFailed update = UpdateOnExecuteFailed.builder().jobId(mainPO.getId()).lastTrigAt(now)
				.exception(new AllInstanceFailedExchangeException(null, null)).build();
		Result2<Boolean, RuntimeException> result2 = delayJobStorage.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// 没到失败阈值

		JobMainVO main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// 最近触发的结果描述是由于All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(false);// 还没结束

		DelayJobPO delayJob = delayJobMapper.findOne(mainPO.getId());
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(0);// 第1次触发后是0

		// ------------------------------------第2次触发
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobStorage.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(false);// 没到失败阈值

		main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// 最近触发的结果描述是由于All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(false);// 还没结束

		delayJob = delayJobMapper.findOne(mainPO.getId());
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(1);// 第2次触发后是1

		// ------------------------------------第3次触发
		now = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now);
		result2 = delayJobStorage.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 到失败阈值

		main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// 最近触发的结果描述是由于All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(true);// 结束
		assertThat(main.getQueued()).isEqualTo(false);// 结束后的不是Queued状态

		delayJob = delayJobMapper.findOne(mainPO.getId());
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// 第3次触发后是2

		// ------------------------------------后续触发返回true，但数据不变
		LocalDateTime now2 = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now2);
		result2 = delayJobStorage.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 到失败阈值

		main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getLastTrigResult()).contains(AllInstanceFailedExchangeException.MESSAGE);// 最近触发的结果描述是由于All
																									// Instance Failed
		assertThat(main.getEnd()).isEqualTo(true);// 结束

		delayJob = delayJobMapper.findOne(mainPO.getId());
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// 还是2
	}

	@Test
	void updateOnExecuteFailed_Exception() {
		// ------------------------------------第1次触发
		LocalDateTime now = LocalDateTime.now().withNano(0);

		UpdateOnExecuteFailed update = UpdateOnExecuteFailed.builder().jobId(mainPO.getId()).lastTrigAt(now)
				.exception(new Exception("ex")).build();
		Result2<Boolean, RuntimeException> result2 = delayJobStorage.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 直接到阈值

		JobMainVO main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getLastTrigResult()).isEqualTo("ex");// 最近触发的结果描述是对应的异常
		assertThat(main.getEnd()).isEqualTo(true);// 结束
		assertThat(main.getQueued()).isEqualTo(false);// 结束后的不是Queued状态

		DelayJobPO delayJob = delayJobMapper.findOne(mainPO.getId());
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// 直接等于2

		// ------------------------------------后续触发返回true，但数据不变
		LocalDateTime now2 = LocalDateTime.now().withNano(0);
		update.setLastTrigAt(now2);
		result2 = delayJobStorage.updateOnExecuteFailed(update);
		assertThat(result2.isSuccess()).isEqualTo(true);
		assertThat(result2.getT1()).isEqualTo(true);// 到失败阈值

		main = jobMainManager.findOne(mainPO.getId(), JobMainQuery.With.WITH_MOST);
		assertThat(main.getLastTrigAt()).isEqualTo(now);// 最近触发的时间
		assertThat(main.getLastTrigResult()).isEqualTo("ex");// 最近触发的结果描述是对应的异常
		assertThat(main.getEnd()).isEqualTo(true);// 结束

		delayJob = delayJobMapper.findOne(mainPO.getId());
		assertThat(delayJob.getRetriedTimesOnExecuteFailed()).isEqualTo(2);// 还是2
	}
}

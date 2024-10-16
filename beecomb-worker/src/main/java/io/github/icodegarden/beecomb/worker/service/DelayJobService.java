package io.github.icodegarden.beecomb.worker.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.DelayJobManager;
import io.github.icodegarden.beecomb.common.backend.manager.JobExecuteRecordManager;
import io.github.icodegarden.beecomb.common.backend.mapper.DelayJobMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobOnExecutedDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.DelayJobVO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteFailedDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteSuccessDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnNoQualifiedExecutorDTO;
import io.github.icodegarden.nutrient.exchange.exception.ExchangeException;
import io.github.icodegarden.nutrient.lang.result.Result1;
import io.github.icodegarden.nutrient.lang.result.Result2;
import io.github.icodegarden.nutrient.lang.result.Results;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Service
public class DelayJobService extends BaseJobService {

	@Autowired
	private DelayJobMapper delayJobMapper;
	@Autowired
	private DelayJobManager delayJobManager;
	@Autowired
	private JobExecuteRecordManager jobExecuteRecordManager;

	@Transactional
	@Override
	public Result2<Boolean, RuntimeException> updateOnNoQualifiedExecutor(UpdateOnNoQualifiedExecutorDTO update) {
		try {
			DelayJobVO delayJob = delayJobManager.findOne(update.getJobId(), null);
			if (delayJob != null) {
				JobMainVO jobMain = jobMainManager.findOne(update.getJobId(), null);
				if (jobMain.getEnd()) {
					return Results.of(true, true/* 到失败阈值 */, null);
				}

				int retriedTimesOnNoQualified = delayJob.getRetriedTimesOnNoQualified();
				DelayJobPO.Update delayUpdate = null;
				if (jobMain.getLastTrigAt() != null) {
					/**
					 * retriedTimesOnNoQualified次数只在非第一次触发时才递增，首次触发不计retry
					 */
					retriedTimesOnNoQualified++;
					delayUpdate = DelayJobPO.Update.builder().jobId(update.getJobId())
							.retriedTimesOnNoQualified(retriedTimesOnNoQualified).build();

					DelayJobPO.Update f = delayUpdate;
					RETRY_TEMPLATE.execute(ctx -> delayJobMapper.update(f));
				}

				DelayBO delay = delayJob.toDelayBO();
				LocalDateTime nextTrigAt = delay.calcNextTrigAtOnNoQualified();

				boolean thresholdReached = false;
				Boolean end = null;
				Boolean queuedAtInstanceNull = null;
				if (retriedTimesOnNoQualified >= delayJob.getRetryOnNoQualified()) {
					/**
					 * 达到阈值则结束
					 */
					end = true;
					queuedAtInstanceNull = true;
					thresholdReached = true;
				}

				UpdateJobOnExecutedDTO mainUpdate = UpdateJobOnExecutedDTO.builder().id(update.getJobId())
						.lastTrigAt(update.getLastTrigAt())
						.lastTrigResult(buildLastTrigResult(update.getNoQualifiedInstanceExchangeException()))
						.nextTrigAt(nextTrigAt).lastExecuteSuccess(false).end(end)
						.queuedAtInstanceNull(queuedAtInstanceNull).build();

				RETRY_TEMPLATE.execute(ctx -> {
					jobExecuteRecordManager.createOnExecuted(InstanceProperties.singleton().getServer().getIpPort(),
							mainUpdate);
					update(mainUpdate);
					return null;
				});

				return Results.of(true, thresholdReached, null);
			}
			return Results.of(false, false, null);
		} catch (RuntimeException e) {
			log.error("ex on updateOnNoQualifiedExecutor, update param:{}", update, e);
			return Results.of(false, false, e);
		}
	}

	@Override
	public Result1<RuntimeException> updateOnExecuteSuccess(UpdateOnExecuteSuccessDTO update) {
		try {
			UpdateJobOnExecutedDTO mainUpdate = UpdateJobOnExecutedDTO.builder().id(update.getJobId())
					.lastTrigAt(update.getLastTrigAt()).lastTrigResult("Success").end(true)
					.lastExecuteExecutor(SystemUtils.formatIpPort(update.getExecutorIp(), update.getExecutorPort()))
					.lastExecuteReturns(update.getLastExecuteReturns()).lastExecuteSuccess(true)
					.queuedAtInstanceNull(true)/* 不需要指定该参数.queued(false) */.build();

			RETRY_TEMPLATE.execute(ctx -> {
				jobExecuteRecordManager.createOnExecuted(InstanceProperties.singleton().getServer().getIpPort(),
						mainUpdate);
				update(mainUpdate);
				return null;
			});

			return Results.of(true, null);
		} catch (RuntimeException e) {
			log.error("ex on updateOnExecuteSuccess, update param:{}", update, e);
			return Results.of(false, e);
		}
	}

	@Transactional
	@Override
	public Result2<Boolean, RuntimeException> updateOnExecuteFailed(UpdateOnExecuteFailedDTO update) {
		try {
			DelayJobVO delayJob = delayJobManager.findOne(update.getJobId(), null);
			if (delayJob != null) {
				JobMainVO jobMain = jobMainManager.findOne(update.getJobId(), null);
				if (jobMain.getEnd()) {
					return Results.of(true, true/* 到失败阈值 */, null);
				}

				if (update.getException() instanceof ExchangeException) {
					int retriedTimesOnExecuteFailed = delayJob.getRetriedTimesOnExecuteFailed();
					DelayJobPO.Update delayUpdate = null;
					if (jobMain.getLastTrigAt() != null) {
						/**
						 * retriedTimesOnExecuteFailed次数只在非第一次触发时才递增，首次触发不计retry
						 */
						retriedTimesOnExecuteFailed++;
						delayUpdate = DelayJobPO.Update.builder().jobId(update.getJobId())
								.retriedTimesOnExecuteFailed(retriedTimesOnExecuteFailed).build();

						DelayJobPO.Update f = delayUpdate;
						RETRY_TEMPLATE.execute(ctx -> delayJobMapper.update(f));
					}

					DelayBO delay = delayJob.toDelayBO();
					LocalDateTime nextTrigAt = delay.calcNextTrigAtOnExecuteFailed();

					boolean thresholdReached = false;
					Boolean end = null;
					Boolean queuedAtInstanceNull = null;
					if (retriedTimesOnExecuteFailed >= delayJob.getRetryOnExecuteFailed()) {
						// 达到阈值则结束
						end = true;
						queuedAtInstanceNull = true;
						thresholdReached = true;
					}

					UpdateJobOnExecutedDTO mainUpdate = UpdateJobOnExecutedDTO.builder().id(update.getJobId())
							.lastTrigAt(update.getLastTrigAt()).lastExecuteSuccess(false)
							.lastTrigResult(buildLastTrigResult(update.getException())).nextTrigAt(nextTrigAt).end(end)
							.queuedAtInstanceNull(queuedAtInstanceNull).build();

					RETRY_TEMPLATE.execute(ctx -> {
						jobExecuteRecordManager.createOnExecuted(InstanceProperties.singleton().getServer().getIpPort(),
								mainUpdate);
						update(mainUpdate);
						return null;
					});

					return Results.of(true, thresholdReached, null);
				} else {
					/**
					 * 非ExchangeException导致的失败，视为不可重试
					 */
					DelayJobPO.Update delayUpdate = DelayJobPO.Update.builder().jobId(update.getJobId())
							.retriedTimesOnExecuteFailed(delayJob.getRetryOnExecuteFailed()).build();
					RETRY_TEMPLATE.execute(ctx -> delayJobMapper.update(delayUpdate));

					UpdateJobOnExecutedDTO mainUpdate = UpdateJobOnExecutedDTO.builder().id(update.getJobId())
							.lastTrigAt(update.getLastTrigAt()).lastExecuteSuccess(false)
							.lastTrigResult(buildLastTrigResult(update.getException())).end(true)
							.queuedAtInstanceNull(true)/* .queued(false) */.build();

					RETRY_TEMPLATE.execute(ctx -> {
						jobExecuteRecordManager.createOnExecuted(InstanceProperties.singleton().getServer().getIpPort(),
								mainUpdate);
						update(mainUpdate);
						return null;
					});

					return Results.of(true, true, null);
				}
			}
			return Results.of(false, false, null);
		} catch (RuntimeException e) {
			log.error("ex on updateOnExecuteFailed, update param:{}", update, e);
			return Results.of(false, false, e);
		}
	}

}

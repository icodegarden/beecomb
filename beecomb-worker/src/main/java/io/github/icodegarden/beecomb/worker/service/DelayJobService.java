package io.github.icodegarden.beecomb.worker.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.mapper.DelayJobMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.transfer.UpdateJobMainOnExecutedDTO;
import io.github.icodegarden.beecomb.common.db.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import io.github.icodegarden.beecomb.worker.core.JobFreshParams;
import io.github.icodegarden.beecomb.worker.manager.JobExecuteRecordManager;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.lang.result.Result1;
import io.github.icodegarden.commons.lang.result.Result2;
import io.github.icodegarden.commons.lang.result.Results;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Service("storage-delay")
public class DelayJobService extends BaseJobService {

	@Autowired
	private DelayJobMapper delayJobMapper;
	@Autowired
	private JobExecuteRecordManager jobExecuteRecordService;

	@Transactional
	@Override
	public Result2<Boolean, RuntimeException> updateOnNoQualifiedExecutor(UpdateOnNoQualifiedExecutor update) {
		try {
			DelayJobPO delayJobPO = delayJobMapper.findOne(update.getJobId());
			if (delayJobPO != null) {
				JobMainVO jobMain = jobMainManager.findOne(update.getJobId(), null);
				if (jobMain.getEnd()) {
					return Results.of(true, true/* 到失败阈值 */, null);
				}

				int retriedTimesOnNoQualified = delayJobPO.getRetriedTimesOnNoQualified();
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

				DelayBO delay = delayJobPO.toDelayBO();
				LocalDateTime nextTrigAt = delay.calcNextTrigAtOnNoQualified();

				boolean thresholdReached = false;
				Boolean end = null;
				if (retriedTimesOnNoQualified >= delayJobPO.getRetryOnNoQualified()) {
					/**
					 * 达到阈值则结束
					 */
					end = true;
					thresholdReached = true;
				}

				UpdateJobMainOnExecutedDTO mainUpdate = UpdateJobMainOnExecutedDTO.builder().id(update.getJobId())
						.lastTrigAt(update.getLastTrigAt())
						.lastTrigResult(buildLastTrigResult(update.getNoQualifiedInstanceExchangeException()))
						.nextTrigAt(nextTrigAt).lastExecuteSuccess(false).end(end).build();

				if (update.getCallback() != null) {
					JobFreshParams params = new JobFreshParams(null, null, false, mainUpdate.getLastTrigAt(),
							mainUpdate.getLastTrigResult());
					update.getCallback().accept(params);
				}

				RETRY_TEMPLATE.execute(ctx -> {
					jobExecuteRecordService.createOnExecuted(mainUpdate);
					jobMainManager.updateOnExecuted(mainUpdate);
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
	public Result1<RuntimeException> updateOnExecuteSuccess(UpdateOnExecuteSuccess update) {
		try {
			UpdateJobMainOnExecutedDTO mainUpdate = UpdateJobMainOnExecutedDTO.builder().id(update.getJobId())
					.lastTrigAt(update.getLastTrigAt()).lastTrigResult("Success").end(true)
					.lastExecuteExecutor(SystemUtils.formatIpPort(update.getExecutorIp(), update.getExecutorPort()))
					.lastExecuteReturns(update.getLastExecuteReturns()).lastExecuteSuccess(true)
					/* 不需要指定该参数.queued(false) */.build();

			if (update.getCallback() != null) {
				JobFreshParams params = new JobFreshParams(mainUpdate.getLastExecuteExecutor(),
						mainUpdate.getLastExecuteReturns(), true, mainUpdate.getLastTrigAt(),
						mainUpdate.getLastTrigResult());
				update.getCallback().accept(params);
			}

			RETRY_TEMPLATE.execute(ctx -> {
				jobExecuteRecordService.createOnExecuted(mainUpdate);
				jobMainManager.updateOnExecuted(mainUpdate);
				return null;
			});

			jobExecuteRecordService.createOnExecuted(mainUpdate);

			return Results.of(true, null);
		} catch (RuntimeException e) {
			log.error("ex on updateOnExecuteSuccess, update param:{}", update, e);
			return Results.of(false, e);
		}
	}

	@Transactional
	@Override
	public Result2<Boolean, RuntimeException> updateOnExecuteFailed(UpdateOnExecuteFailed update) {
		try {
			DelayJobPO delayJobPO = delayJobMapper.findOne(update.getJobId());
			if (delayJobPO != null) {
				JobMainVO jobMain = jobMainManager.findOne(update.getJobId(), null);
				if (jobMain.getEnd()) {
					return Results.of(true, true/* 到失败阈值 */, null);
				}

				if (update.getException() instanceof ExchangeException) {
					int retriedTimesOnExecuteFailed = delayJobPO.getRetriedTimesOnExecuteFailed();
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

					DelayBO delay = delayJobPO.toDelayBO();
					LocalDateTime nextTrigAt = delay.calcNextTrigAtOnExecuteFailed();

					boolean thresholdReached = false;
					Boolean end = null;
					if (retriedTimesOnExecuteFailed >= delayJobPO.getRetryOnExecuteFailed()) {
						// 达到阈值则结束
						end = true;
						thresholdReached = true;
					}

					UpdateJobMainOnExecutedDTO mainUpdate = UpdateJobMainOnExecutedDTO.builder().id(update.getJobId())
							.lastTrigAt(update.getLastTrigAt()).lastExecuteSuccess(false)
							.lastTrigResult(buildLastTrigResult(update.getException())).nextTrigAt(nextTrigAt).end(end)
							.build();

					if (update.getCallback() != null) {
						JobFreshParams params = new JobFreshParams(null, null, false, mainUpdate.getLastTrigAt(),
								mainUpdate.getLastTrigResult());
						update.getCallback().accept(params);
					}

					RETRY_TEMPLATE.execute(ctx -> {
						jobExecuteRecordService.createOnExecuted(mainUpdate);
						jobMainManager.updateOnExecuted(mainUpdate);
						return null;
					});

					return Results.of(true, thresholdReached, null);
				} else {
					/**
					 * 非ExchangeException导致的失败，视为不可重试
					 */
					DelayJobPO.Update delayUpdate = DelayJobPO.Update.builder().jobId(update.getJobId())
							.retriedTimesOnExecuteFailed(delayJobPO.getRetryOnExecuteFailed()).build();
					RETRY_TEMPLATE.execute(ctx -> delayJobMapper.update(delayUpdate));

					UpdateJobMainOnExecutedDTO mainUpdate = UpdateJobMainOnExecutedDTO.builder().id(update.getJobId())
							.lastTrigAt(update.getLastTrigAt()).lastExecuteSuccess(false)
							.lastTrigResult(buildLastTrigResult(update.getException())).end(true)
							/* .queued(false) */.build();

					if (update.getCallback() != null) {
						JobFreshParams params = new JobFreshParams(null, null, false, mainUpdate.getLastTrigAt(),
								mainUpdate.getLastTrigResult());
						update.getCallback().accept(params);
					}

					RETRY_TEMPLATE.execute(ctx -> {
						jobExecuteRecordService.createOnExecuted(mainUpdate);
						jobMainManager.updateOnExecuted(mainUpdate);
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

package io.github.icodegarden.beecomb.worker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.mapper.ScheduleJobMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO.Update;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.ScheduleJobPO;
import io.github.icodegarden.beecomb.worker.core.JobFreshParams;
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
@Service("storage-schedule")
public class ScheduleJobStorage extends BaseJobStorage {

	@Autowired
	private ScheduleJobMapper scheduleJobMapper;
	@Autowired
	private JobExecuteRecordService jobExecuteRecordService;

	@Transactional
	@Override
	public Result2<Boolean, RuntimeException> updateOnNoQualifiedExecutor(UpdateOnNoQualifiedExecutor update) {
		try {
			if (update.getNextTrigAt() == null) {
				return Results.of(false, false, new IllegalArgumentException("nextTrigAt must not null"));
			}
			Update mainUpdate = Update.builder().id(update.getJobId()).lastTrigAt(update.getLastTrigAt())
					.lastTrigResult(buildLastTrigResult(update.getNoQualifiedInstanceExchangeException()))
					.nextTrigAt(update.getNextTrigAt()).build();

			RETRY_TEMPLATE.execute(ctx -> {
				jobExecuteRecordService.createOnJobUpdate(mainUpdate);
				boolean b = jobMainMapper.update(mainUpdate) == 1;
				if (b) {
					ScheduleJobPO.Update scheduleUpdate = ScheduleJobPO.Update.builder().jobId(update.getJobId())
							.build();
					scheduleJobMapper.updateAndIncrementScheduledTimes(scheduleUpdate);
				}

				return null;
			});

			if (update.getCallback() != null) {
				JobFreshParams params = new JobFreshParams(null, null, false, mainUpdate.getLastTrigAt(),
						mainUpdate.getLastTrigResult());
				update.getCallback().accept(params);
			}

			return Results.of(true, false, null);
		} catch (RuntimeException e) {
			log.error("ex on updateOnNoQualifiedExecutor, update param:{}", update, e);
			return Results.of(false, false, e);
		}
	}

	@Transactional
	@Override
	public Result1<RuntimeException> updateOnExecuteSuccess(UpdateOnExecuteSuccess update) {
		try {
			if (update.getNextTrigAt() == null) {
				return Results.of(false, new IllegalArgumentException("nextTrigAt must not null"));
			}
			Update mainUpdate = Update.builder().id(update.getJobId()).lastTrigAt(update.getLastTrigAt())
					.lastTrigResult("Success").end(update.getEnd())
					.lastExecuteExecutor(SystemUtils.formatIpPort(update.getExecutorIp(), update.getExecutorPort()))
					.lastExecuteReturns(update.getLastExecuteReturns()).lastExecuteSuccess(true)
					.nextTrigAt(update.getNextTrigAt()).build();

			RETRY_TEMPLATE.execute(ctx -> {
				jobExecuteRecordService.createOnJobUpdate(mainUpdate);
				boolean b = jobMainMapper.update(mainUpdate) == 1;
				if (b) {
					ScheduleJobPO.Update scheduleUpdate = ScheduleJobPO.Update.builder().jobId(update.getJobId())
							.build();
					scheduleJobMapper.updateAndIncrementScheduledTimes(scheduleUpdate);
				}

				return null;
			});

			if (update.getCallback() != null) {
				JobFreshParams params = new JobFreshParams(mainUpdate.getLastExecuteExecutor(),
						mainUpdate.getLastExecuteReturns(), true, mainUpdate.getLastTrigAt(),
						mainUpdate.getLastTrigResult());
				update.getCallback().accept(params);
			}

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
			if (update.getNextTrigAt() == null) {
				return Results.of(false, false, new IllegalArgumentException("nextTrigAt must not null"));
			}
			Update mainUpdate = Update.builder().id(update.getJobId()).lastTrigAt(update.getLastTrigAt())
					.lastTrigResult(buildLastTrigResult(update.getException())).nextTrigAt(update.getNextTrigAt())
					.build();

			RETRY_TEMPLATE.execute(ctx -> {
				jobExecuteRecordService.createOnJobUpdate(mainUpdate);
				boolean b = jobMainMapper.update(mainUpdate) == 1;
				if (b) {
					ScheduleJobPO.Update scheduleUpdate = ScheduleJobPO.Update.builder().jobId(update.getJobId())
							.build();
					scheduleJobMapper.updateAndIncrementScheduledTimes(scheduleUpdate);
				}

				return null;
			});

			if (update.getCallback() != null) {
				JobFreshParams params = new JobFreshParams(null, null, false, mainUpdate.getLastTrigAt(),
						mainUpdate.getLastTrigResult());
				update.getCallback().accept(params);
			}

			return Results.of(true, false, null);
		} catch (RuntimeException e) {
			log.error("ex on updateOnExecuteFailed, update param:{}", update, e);
			return Results.of(false, false, e);
		}
	}

}

package io.github.icodegarden.beecomb.worker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.JobExecuteRecordManager;
import io.github.icodegarden.beecomb.common.backend.mapper.ScheduleJobMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.ScheduleJobPO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobOnExecutedDTO;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteFailedDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteSuccessDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnNoQualifiedExecutorDTO;
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
public class ScheduleJobService extends BaseJobService {

	@Autowired
	private ScheduleJobMapper scheduleJobMapper;
	@Autowired
	private JobExecuteRecordManager jobExecuteRecordManager;

	@Transactional
	@Override
	public Result2<Boolean, RuntimeException> updateOnNoQualifiedExecutor(UpdateOnNoQualifiedExecutorDTO update) {
		try {
			if (update.getNextTrigAt() == null) {
				return Results.of(false, false, new IllegalArgumentException("nextTrigAt must not null"));
			}
			UpdateJobOnExecutedDTO mainUpdate = UpdateJobOnExecutedDTO.builder().id(update.getJobId())
					.lastTrigAt(update.getLastTrigAt()).lastExecuteSuccess(false)
					.lastTrigResult(buildLastTrigResult(update.getNoQualifiedInstanceExchangeException()))
					.nextTrigAt(update.getNextTrigAt()).build();

			RETRY_TEMPLATE.execute(ctx -> {
				jobExecuteRecordManager.createOnExecuted(InstanceProperties.singleton().getServer().getIpPort(),
						mainUpdate);
				boolean b = update(mainUpdate);
				if (b) {
					ScheduleJobPO.Update scheduleUpdate = ScheduleJobPO.Update.builder().jobId(update.getJobId())
							.build();
					scheduleJobMapper.updateAndIncrementScheduledTimes(scheduleUpdate);
				}

				return null;
			});

			return Results.of(true, false, null);
		} catch (RuntimeException e) {
			log.error("ex on updateOnNoQualifiedExecutor, update param:{}", update, e);
			return Results.of(false, false, e);
		}
	}

	@Transactional
	@Override
	public Result1<RuntimeException> updateOnExecuteSuccess(UpdateOnExecuteSuccessDTO update) {
		try {
			if (update.getNextTrigAt() == null) {
				return Results.of(false, new IllegalArgumentException("nextTrigAt must not null"));
			}
			UpdateJobOnExecutedDTO mainUpdate = UpdateJobOnExecutedDTO.builder().id(update.getJobId())
					.lastTrigAt(update.getLastTrigAt()).lastTrigResult("Success").end(update.getEnd())
					.lastExecuteExecutor(SystemUtils.formatIpPort(update.getExecutorIp(), update.getExecutorPort()))
					.lastExecuteReturns(update.getLastExecuteReturns()).lastExecuteSuccess(true)
					.nextTrigAt(update.getNextTrigAt())
					.queuedAtInstanceNull(Boolean.TRUE.equals(update.getEnd()) ? true : null).build();

			RETRY_TEMPLATE.execute(ctx -> {
				jobExecuteRecordManager.createOnExecuted(InstanceProperties.singleton().getServer().getIpPort(),
						mainUpdate);
				boolean b = update(mainUpdate);
				if (b) {
					ScheduleJobPO.Update scheduleUpdate = ScheduleJobPO.Update.builder().jobId(update.getJobId())
							.build();
					scheduleJobMapper.updateAndIncrementScheduledTimes(scheduleUpdate);
				}

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
			if (update.getNextTrigAt() == null) {
				return Results.of(false, false, new IllegalArgumentException("nextTrigAt must not null"));
			}
			UpdateJobOnExecutedDTO mainUpdate = UpdateJobOnExecutedDTO.builder().id(update.getJobId())
					.lastTrigAt(update.getLastTrigAt()).lastExecuteSuccess(false)
					.lastTrigResult(buildLastTrigResult(update.getException())).nextTrigAt(update.getNextTrigAt())
					.build();

			RETRY_TEMPLATE.execute(ctx -> {
				jobExecuteRecordManager.createOnExecuted(InstanceProperties.singleton().getServer().getIpPort(),
						mainUpdate);
				boolean b = update(mainUpdate);
				if (b) {
					ScheduleJobPO.Update scheduleUpdate = ScheduleJobPO.Update.builder().jobId(update.getJobId())
							.build();
					scheduleJobMapper.updateAndIncrementScheduledTimes(scheduleUpdate);
				}

				return null;
			});

			return Results.of(true, false, null);
		} catch (RuntimeException e) {
			log.error("ex on updateOnExecuteFailed, update param:{}", update, e);
			return Results.of(false, false, e);
		}
	}

}

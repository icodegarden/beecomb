package io.github.icodegarden.beecomb.test;

import java.util.UUID;

import io.github.icodegarden.beecomb.common.backend.manager.DelayJobManager;
import io.github.icodegarden.beecomb.common.backend.manager.JobDetailManager;
import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.manager.ScheduleJobManager;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobDetailDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobMainDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateScheduleJobDTO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.nursery.springboot.SpringContext;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class PrepareDatasUtils {

	public static void createCompleteJob() throws Exception {
		CreateJobMainDTO dto = new CreateJobMainDTO();
		dto.setName("myjob");
		dto.setUuid(UUID.randomUUID().toString());
		dto.setType(JobType.Delay);
		dto.setExecutorName("n1");
		dto.setJobHandlerName("j1");
		dto.setCreatedBy("beecomb");

		JobMainManager jobMainManager = SpringContext.getApplicationContext().getBean(JobMainManager.class);

		jobMainManager.create(dto);

		Long jobId = dto.getId();

		JobDetailManager jobDetailManager = SpringContext.getApplicationContext().getBean(JobDetailManager.class);

		CreateJobDetailDTO createJobDetailDTO = new CreateJobDetailDTO();
		createJobDetailDTO.setJobId(jobId);
		createJobDetailDTO.setDesc("desc");
		createJobDetailDTO.setParams("params");
		jobDetailManager.create(createJobDetailDTO);

		DelayJobManager delayJobManager = SpringContext.getApplicationContext().getBean(DelayJobManager.class);

		CreateDelayJobDTO delay = new CreateDelayJobDTO();
		delay.setJobId(jobId);
		delay.setDelay(5000L);
		delay.setRetryOnExecuteFailed(3);
		delay.setRetryBackoffOnExecuteFailed(3000);
		delay.setRetryOnNoQualified(5);
		delay.setRetryBackoffOnNoQualified(5000);

		delayJobManager.create(delay);

		ScheduleJobManager scheduleJobManager = SpringContext.getApplicationContext().getBean(ScheduleJobManager.class);

		CreateScheduleJobDTO createScheduleJobDTO = new CreateScheduleJobDTO();
		createScheduleJobDTO.setJobId(jobId);
		createScheduleJobDTO.setSheduleCron("0 * * * * *");

		scheduleJobManager.create(createScheduleJobDTO);
	}

}
package io.github.icodegarden.beecomb.client;

import java.io.IOException;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.client.pojo.query.JobQuery;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateScheduleJobDTO;
import io.github.icodegarden.beecomb.client.pojo.transfer.UpdateJobDTO;
import io.github.icodegarden.beecomb.client.pojo.transfer.UpdateJobDTO.Delay;
import io.github.icodegarden.beecomb.client.pojo.transfer.UpdateJobDTO.Schedule;
import io.github.icodegarden.beecomb.client.pojo.view.CreateJobVO;
import io.github.icodegarden.beecomb.client.pojo.view.DeleteJobVO;
import io.github.icodegarden.beecomb.client.pojo.view.JobVO;
import io.github.icodegarden.beecomb.client.pojo.view.PageVO;
import io.github.icodegarden.beecomb.client.pojo.view.UpdateJobVO;
import io.github.icodegarden.beecomb.client.security.BasicAuthentication;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.test.Properties4Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
abstract class AbstractBeeCombClientTests extends Properties4Test {

	BasicAuthentication authentication = new BasicAuthentication("beecomb", "beecomb");
	BeeCombClient beeCombClient;

	@BeforeEach
	void init() {
		beeCombClient = getBeeCombClient();
	}

	@AfterEach
	void close() throws IOException {
		beeCombClient.close();
	}

	protected abstract BeeCombClient getBeeCombClient();

	@Test
	void createJob() throws Exception {
		CreateDelayJobDTO dto = new CreateDelayJobDTO("job", "executorName", "jobHandlerName",
				new CreateDelayJobDTO.Delay(1000L));
		dto.setUuid(UUID.randomUUID().toString());
		CreateJobVO response = beeCombClient.createJob(dto);

		Assertions.assertThat(response.getJob()).isNotNull();
		Assertions.assertThat(response.getJob().getId()).isNotNull();
		Assertions.assertThat(response.getJob().getName()).isEqualTo("job");
		Assertions.assertThat(response.getJob().getPriority()).isNotNull();
		Assertions.assertThat(response.getJob().getQueued()).isNotNull();
		Assertions.assertThat(response.getJob().getUuid()).isEqualTo(dto.getUuid());
		Assertions.assertThat(response.getJob().getWeight()).isNotNull();
		Assertions.assertThat(response.getJob().getType()).isEqualTo(JobType.Delay);
	}

	@Test
	void pageJobs() throws Exception {
		CreateDelayJobDTO dto = new CreateDelayJobDTO("job", "executorName", "jobHandlerName",
				new CreateDelayJobDTO.Delay(1000L));
		CreateJobVO response1 = beeCombClient.createJob(dto);
//		CreateJobResponse response2 = beeCombClient.createJob(dto);

		JobQuery query = new JobQuery();
		query.setSize(1);
		PageVO<JobVO> page = beeCombClient.pageJobs(query);

		Assertions.assertThat(page).isNotNull();
		Assertions.assertThat(page.getResult()).isNotEmpty();
//		Assertions.assertThat(page.getTotalPages()).isEqualTo(2);//每页1条共2页
//		Assertions.assertThat(page.getTotalCount()).isEqualTo(2);
	}

	@Test
	void findJob() throws Exception {
		CreateDelayJobDTO dto = new CreateDelayJobDTO("job", "executorName", "jobHandlerName",
				new CreateDelayJobDTO.Delay(1000L));
		CreateJobVO response = beeCombClient.createJob(dto);

		JobVO findJob = beeCombClient.getJob(response.getJob().getId());

		Assertions.assertThat(findJob).isNotNull();
		Assertions.assertThat(findJob.getId()).isEqualTo(response.getJob().getId());
	}

	@Test
	void findJobByUUID() throws Exception {
		CreateDelayJobDTO dto = new CreateDelayJobDTO("job", "executorName", "jobHandlerName",
				new CreateDelayJobDTO.Delay(1000L));
		dto.setUuid(UUID.randomUUID().toString());
		CreateJobVO response = beeCombClient.createJob(dto);

		JobVO findJob = beeCombClient.getJobByUUID(response.getJob().getUuid());

		Assertions.assertThat(findJob).isNotNull();
		Assertions.assertThat(findJob.getId()).isEqualTo(response.getJob().getId());
		Assertions.assertThat(findJob.getUuid()).isEqualTo(response.getJob().getUuid());
	}

	@Test
	void updateJob_delay() throws Exception {
		CreateDelayJobDTO dto = new CreateDelayJobDTO("job", "executorName", "jobHandlerName",
				new CreateDelayJobDTO.Delay(1000L));
		CreateJobVO response = beeCombClient.createJob(dto);

		Long jobId = response.getJob().getId();
		JobVO findJob = beeCombClient.getJob(jobId);

		UpdateJobDTO update = new UpdateJobDTO(findJob.getId());
		update.setDesc("desc2");
		update.setExecuteTimeout(1999);
		update.setExecutorName("executorName2");
		update.setJobHandlerName("jobHandlerName2");
		update.setMaxParallelShards(19);
		update.setName("name2");
		update.setParams("params2");
		update.setPriority(9);
		update.setWeight(4);
		Delay delay = new UpdateJobDTO.Delay();
		delay.setDelay(15000L);
		update.setDelay(delay);
		UpdateJobVO vo = beeCombClient.updateJob(update);

		Assertions.assertThat(vo.getId()).isEqualTo(jobId);
		Assertions.assertThat(vo.getSuccess()).isTrue();

		findJob = beeCombClient.getJob(response.getJob().getId());

		Assertions.assertThat(findJob.getDesc()).isEqualTo("desc2");
		Assertions.assertThat(findJob.getExecuteTimeout()).isEqualTo(1999);
		Assertions.assertThat(findJob.getExecutorName()).isEqualTo("executorName2");
		Assertions.assertThat(findJob.getJobHandlerName()).isEqualTo("jobHandlerName2");
		Assertions.assertThat(findJob.getMaxParallelShards()).isEqualTo(19);
		Assertions.assertThat(findJob.getName()).isEqualTo("name2");
		Assertions.assertThat(findJob.getParams()).isEqualTo("params2");
		Assertions.assertThat(findJob.getPriority()).isEqualTo(9);
		Assertions.assertThat(findJob.getWeight()).isEqualTo(4);
		Assertions.assertThat(findJob.getDelay().getDelay()).isEqualTo(15000);
	}

	@Test
	void updateJob_schedule() throws Exception {
		CreateScheduleJobDTO dto = new CreateScheduleJobDTO("job", "executorName", "jobHandlerName",
				CreateScheduleJobDTO.Schedule.scheduleFixDelay(3000L));

		CreateJobVO response = beeCombClient.createJob(dto);

		Long jobId = response.getJob().getId();
		JobVO findJob = beeCombClient.getJob(jobId);

		UpdateJobDTO update = new UpdateJobDTO(findJob.getId());
		Schedule schedule = new UpdateJobDTO.Schedule();
		schedule.setScheduleFixDelay(15000L);
		update.setSchedule(schedule);
		UpdateJobVO vo = beeCombClient.updateJob(update);

		Assertions.assertThat(vo.getId()).isEqualTo(jobId);
		Assertions.assertThat(vo.getSuccess()).isTrue();

		findJob = beeCombClient.getJob(response.getJob().getId());

		Assertions.assertThat(findJob.getSchedule().getScheduleFixDelay()).isEqualTo(15000);
	}

	@Test
	void deleteJob() throws Exception {
		CreateDelayJobDTO dto = new CreateDelayJobDTO("job", "executorName", "jobHandlerName",
				new CreateDelayJobDTO.Delay(1000L));
		CreateJobVO response = beeCombClient.createJob(dto);

		Long jobId = response.getJob().getId();
		DeleteJobVO vo = beeCombClient.deleteJob(jobId);

		Assertions.assertThat(vo.getId()).isEqualTo(jobId);
		Assertions.assertThat(vo.getSuccess()).isTrue();
	}
}

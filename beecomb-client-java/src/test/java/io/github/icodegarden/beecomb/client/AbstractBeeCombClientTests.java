package io.github.icodegarden.beecomb.client;

import java.io.IOException;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.client.pojo.request.JobQuery;
import io.github.icodegarden.beecomb.client.pojo.response.CreateJobResponse;
import io.github.icodegarden.beecomb.client.pojo.response.FindJobResponse;
import io.github.icodegarden.beecomb.client.pojo.response.PageResponse;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.client.security.BasicAuthentication;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.test.PropertiesConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
abstract class AbstractBeeCombClientTests extends PropertiesConfig {

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
				new CreateDelayJobDTO.Delay(1000));
		dto.setUuid(UUID.randomUUID().toString());
		CreateJobResponse response = beeCombClient.createJob(dto);

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
				new CreateDelayJobDTO.Delay(1000));
		CreateJobResponse response1 = beeCombClient.createJob(dto);
//		CreateJobResponse response2 = beeCombClient.createJob(dto);

		JobQuery query = new JobQuery();
		query.setSize(1);
		PageResponse<FindJobResponse> page = beeCombClient.pageJobs(query);

		Assertions.assertThat(page).isNotNull();
		Assertions.assertThat(page.getResult()).isNotEmpty();
//		Assertions.assertThat(page.getTotalPages()).isEqualTo(2);//每页1条共2页
//		Assertions.assertThat(page.getTotalCount()).isEqualTo(2);
	}

	@Test
	void findJob() throws Exception {
		CreateDelayJobDTO dto = new CreateDelayJobDTO("job", "executorName", "jobHandlerName",
				new CreateDelayJobDTO.Delay(1000));
		CreateJobResponse response = beeCombClient.createJob(dto);

		FindJobResponse findJob = beeCombClient.findJob(response.getJob().getId());

		Assertions.assertThat(findJob).isNotNull();
		Assertions.assertThat(findJob.getId()).isEqualTo(response.getJob().getId());
	}

	@Test
	void findJobByUUID() throws Exception {
		CreateDelayJobDTO dto = new CreateDelayJobDTO("job", "executorName", "jobHandlerName",
				new CreateDelayJobDTO.Delay(1000));
		dto.setUuid(UUID.randomUUID().toString());
		CreateJobResponse response = beeCombClient.createJob(dto);

		FindJobResponse findJob = beeCombClient.findJobByUUID(response.getJob().getUuid());

		Assertions.assertThat(findJob).isNotNull();
		Assertions.assertThat(findJob.getId()).isEqualTo(response.getJob().getId());
		Assertions.assertThat(findJob.getUuid()).isEqualTo(response.getJob().getUuid());
	}

}

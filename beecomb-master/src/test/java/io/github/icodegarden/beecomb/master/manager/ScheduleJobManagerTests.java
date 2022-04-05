package io.github.icodegarden.beecomb.master.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.manager.ScheduleJobManager;
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.CreateJobOpenapiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.CreateJobOpenapiDTO.Schedule;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class ScheduleJobManagerTests {

	@Autowired
	ScheduleJobManager scheduleJobService;

	@Test
	void create() {
		CreateJobOpenapiDTO dto = new CreateJobOpenapiDTO();
		dto.setName("myjob");
		dto.setType(JobType.Schedule);
		dto.setExecutorName("n1");
		dto.setJobHandlerName("j1");

		Schedule schedule = new CreateJobOpenapiDTO.Schedule();
		schedule.setSheduleCron("0 * * * * *");
		dto.setSchedule(schedule);

		ExecutableJobBO jobVO = scheduleJobService.create(dto);

		assertThat(jobVO).isNotNull();
	}
}

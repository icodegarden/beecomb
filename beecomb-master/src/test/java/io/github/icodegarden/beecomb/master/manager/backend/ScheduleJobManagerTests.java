package io.github.icodegarden.beecomb.master.manager.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.ScheduleJobManager;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateScheduleJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.ScheduleJobVO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class ScheduleJobManagerTests {

	@Autowired
	ScheduleJobManager scheduleJobManager;

	@Test
	void create() {
		Long jobId = 1L;

		CreateScheduleJobDTO dto = new CreateScheduleJobDTO();
		dto.setSheduleCron("0 * * * * *");
		dto.setJobId(jobId);

		scheduleJobManager.create(dto);

		ScheduleJobVO scheduleJobVO = scheduleJobManager.findOne(jobId, null);

		assertThat(scheduleJobVO).isNotNull();

		// jobId重复-----------------------------------------
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleJobManager.create(dto));
	}
}

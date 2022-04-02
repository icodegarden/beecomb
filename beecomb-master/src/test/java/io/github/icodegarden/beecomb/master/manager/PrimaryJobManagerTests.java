package io.github.icodegarden.beecomb.master.manager;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.master.manager.DelayJobManager;
import io.github.icodegarden.beecomb.master.manager.PrimaryJobManager;
import io.github.icodegarden.beecomb.master.manager.ScheduleJobManager;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class PrimaryJobManagerTests {

	@Autowired
	private PrimaryJobManager primaryJobService;
	@MockBean
	private DelayJobManager delayJobService;
	@MockBean
	private ScheduleJobManager scheduleJobService;

	@Test
	void create() {
		CreateJobDTO delayJobDTO = new CreateJobDTO();
		delayJobDTO.setName("myjob1");
		delayJobDTO.setType(JobType.Delay);
		primaryJobService.create(delayJobDTO);

		verify(delayJobService, times(1)).create(delayJobDTO);
		verify(scheduleJobService, times(0)).create(delayJobDTO);

		CreateJobDTO scheduleJobDTO = new CreateJobDTO();
		scheduleJobDTO.setName("myjob2");
		scheduleJobDTO.setType(JobType.Schedule);
		primaryJobService.create(scheduleJobDTO);

		verify(delayJobService, times(0)).create(scheduleJobDTO);
		verify(scheduleJobService, times(1)).create(scheduleJobDTO);
	}

}

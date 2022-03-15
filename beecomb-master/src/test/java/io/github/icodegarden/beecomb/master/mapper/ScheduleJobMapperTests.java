package io.github.icodegarden.beecomb.master.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.mapper.ScheduleJobMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.ScheduleJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.ScheduleJobPO.Update;
/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class ScheduleJobMapperTests {

	@Autowired
	ScheduleJobMapper scheduleJobMapper;

	ScheduleJobPO create() {
		ScheduleJobPO scheduleJobPO = new ScheduleJobPO();
		scheduleJobPO.setJobId(100L);
		scheduleJobPO.setScheduledTimes(1500L);
		scheduleJobPO.setScheduleFixDelay(60000);
		scheduleJobPO.setScheduleFixRate(70000);
		scheduleJobPO.setSheduleCron("0 * * * * *");

		scheduleJobMapper.add(scheduleJobPO);
		return scheduleJobPO;
	}

	@Test
	void add() {
		ScheduleJobPO po = create();
		assertThat(po).isNotNull();
	}

	@Test
	void findOne() {
		ScheduleJobPO po = create();
		ScheduleJobPO findOne = scheduleJobMapper.findOne(po.getJobId());

		assertThat(findOne).isNotNull();
		assertThat(findOne.getJobId()).isEqualTo(po.getJobId());
		assertThat(findOne.getScheduledTimes()).isEqualTo(1500);
		assertThat(findOne.getScheduleFixDelay()).isEqualTo(60000);
		assertThat(findOne.getScheduleFixRate()).isEqualTo(70000);
		assertThat(findOne.getSheduleCron()).isEqualTo("0 * * * * *");
	}

	@Test
	void update() {
		ScheduleJobPO po = create();
		Update update = ScheduleJobPO.Update.builder().jobId(po.getJobId()).scheduleFixDelay(10000)
				.scheduleFixRate(8000).sheduleCron("newcron").build();
		scheduleJobMapper.update(update);
		ScheduleJobPO findOne = scheduleJobMapper.findOne(po.getJobId());

		assertThat(findOne).isNotNull();
		assertThat(findOne.getJobId()).isEqualTo(po.getJobId());
		assertThat(findOne.getScheduleFixDelay()).isEqualTo(10000);
		assertThat(findOne.getScheduleFixRate()).isEqualTo(8000);
		assertThat(findOne.getSheduleCron()).isEqualTo("newcron");
	}
}

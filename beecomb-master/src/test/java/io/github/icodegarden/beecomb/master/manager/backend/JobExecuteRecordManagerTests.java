package io.github.icodegarden.beecomb.master.manager.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.JobExecuteRecordManager;
import io.github.icodegarden.beecomb.common.backend.mapper.JobExecuteRecordMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.data.JobExecuteRecordDO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobExecuteRecordQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobExecuteRecordDTO;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class JobExecuteRecordManagerTests {

	@Autowired
	private JobExecuteRecordManager jobExecuteRecordManager;
	@Autowired
	private JobExecuteRecordMapper jobExecuteRecordMapper;

//	JobMainPO mainPO;
//
//	JobMainPO create() {
//		mainPO = new JobMainPO();
//		mainPO.setCreatedBy("FangfangXu");
//		mainPO.setName("myjob");
//		mainPO.setPriority(5);
//		mainPO.setType(JobType.Delay);
//		mainPO.setWeight(1);
//		mainPO.setExecutorName("n");
//		mainPO.setJobHandlerName("j");
//		mainPO.setQueued(true);
//		jobMainMapper.add(mainPO);
//		return mainPO;
//	}

	@BeforeEach
	void init() {
	}

	@Test
	void create() {
		CreateJobExecuteRecordDTO dto = new CreateJobExecuteRecordDTO();
		dto.setExecuteExecutor("1.1.1.1:11");
		dto.setExecuteReturns("executeReturns");
		dto.setJobId(1L);
		dto.setSuccess(true);
		dto.setTrigAt(SystemUtils.now());
		dto.setTrigResult("rrr");

		jobExecuteRecordManager.createRecord(dto);

		JobExecuteRecordDO record = jobExecuteRecordMapper.findAll(JobExecuteRecordQuery.builder().build()).get(0);

		assertThat(record).isNotNull();
	}
}

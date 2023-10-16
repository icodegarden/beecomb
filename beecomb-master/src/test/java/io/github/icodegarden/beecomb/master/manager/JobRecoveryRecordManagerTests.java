package io.github.icodegarden.beecomb.master.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobMainDTO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordQuery;
import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordQuery.With;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateOrUpdateJobRecoveryRecordDTO;
import io.github.icodegarden.beecomb.master.pojo.view.JobRecoveryRecordVO;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class JobRecoveryRecordManagerTests {

	@Autowired
	JobRecoveryRecordManager jobRecoveryRecordService;
	@Autowired
	JobMainManager jobMainManager;

	void create(Long jobId) {
		CreateOrUpdateJobRecoveryRecordDTO dto = new CreateOrUpdateJobRecoveryRecordDTO();
		dto.setDesc("ok");
		dto.setJobId(jobId);
		dto.setRecoveryAt(SystemUtils.now());
		dto.setSuccess(true);
		jobRecoveryRecordService.createOrUpdate(dto);
	}

	private CreateJobMainDTO createJobMain() {
		CreateJobMainDTO dto = new CreateJobMainDTO();
		dto.setName("myjob");
		dto.setUuid(UUID.randomUUID().toString());
		dto.setType(JobType.Delay);
		dto.setExecutorName("n1");
		dto.setJobHandlerName("j1");

		jobMainManager.create(dto);
		return dto;
	}

	@Test
	void page() {
		create(1L);

		JobRecoveryRecordQuery query = JobRecoveryRecordQuery.builder().build();
		Page<JobRecoveryRecordVO> page = jobRecoveryRecordService.page(query);

		assertThat(page.getResult().size()).isGreaterThanOrEqualTo(1);
	}

	@Test
	void findOne() {
		/**
		 * 由于这里WITH_MOST会使用join，所以需要job数据
		 */
		CreateJobMainDTO createJobMainDTO = createJobMain();

		create(createJobMainDTO.getId());

		With with = JobRecoveryRecordQuery.With.WITH_MOST;
		JobRecoveryRecordVO findOne = jobRecoveryRecordService.findOne(createJobMainDTO.getId(), with);
		assertThat(findOne).isNotNull();
	}

}

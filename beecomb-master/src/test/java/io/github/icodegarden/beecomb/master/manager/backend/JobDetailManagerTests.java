package io.github.icodegarden.beecomb.master.manager.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.JobDetailManager;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobDetailDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobDetailVO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class JobDetailManagerTests {

	@Autowired
	JobDetailManager jobDetailManager;

	@Test
	void create() throws InterruptedException {
		Long jobId = 1L;

		CreateJobDetailDTO dto = new CreateJobDetailDTO();
		dto.setDesc("desc");
		dto.setJobId(jobId);
		dto.setParams("params");
		jobDetailManager.create(dto);

		JobDetailVO jobDetailVO = jobDetailManager.findOne(jobId, null);

		assertThat(jobDetailVO).isNotNull();

		// jobId重复-----------------------------------------
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> jobDetailManager.create(dto));
	}
}

package io.github.icodegarden.beecomb.master.manager.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobMainDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.enums.JobType;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class JobMainManagerTests {

	@Autowired
	JobMainManager jobMainManager;

	@Test
	void create() throws Exception {
		CreateJobMainDTO dto = new CreateJobMainDTO();
		dto.setName("myjob");
		dto.setUuid(UUID.randomUUID().toString());
		dto.setType(JobType.Delay);
		dto.setExecutorName("n1");
		dto.setJobHandlerName("j1");
		dto.setCreatedBy("beecomb");

		jobMainManager.create(dto);

		JobMainVO jobMainVO = jobMainManager.findOne(dto.getId(), null);

		assertThat(jobMainVO).isNotNull();

		// id重复,因为第二次dto已经有id-----------------------------------------
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> jobMainManager.create(dto));
	}
}

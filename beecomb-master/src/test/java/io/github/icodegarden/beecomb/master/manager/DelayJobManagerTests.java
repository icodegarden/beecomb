package io.github.icodegarden.beecomb.master.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.manager.DelayJobManager;
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.CreateJobOpenapiDTO;
/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class DelayJobManagerTests {

	@Autowired
	DelayJobManager delayJobService;

	@Test
	void create() throws InterruptedException {
		CreateJobOpenapiDTO dto = new CreateJobOpenapiDTO();
		dto.setName("myjob");
		dto.setUuid(UUID.randomUUID().toString());
		dto.setType(JobType.Delay);
		dto.setExecutorName("n1");
		dto.setJobHandlerName("j1");

		CreateJobOpenapiDTO.Delay delay = new CreateJobOpenapiDTO.Delay();
		delay.setDelay(5000);
		delay.setRetryOnExecuteFailed(3);
		delay.setRetryBackoffOnExecuteFailed(3000);
		delay.setRetryOnNoQualified(5);
		delay.setRetryBackoffOnNoQualified(5000);
		
		dto.setDelay(delay);

		ExecutableJobBO job = delayJobService.create(dto);

		assertThat(job).isNotNull();
		
		DelayBO d = job.getDelay();
		assertThat(d).isNotNull();
		assertThat(d.getDelay()).isNotNull();
		assertThat(d.getRetryOnExecuteFailed()).isEqualTo(3);
		assertThat(d.getRetryBackoffOnExecuteFailed()).isEqualTo(3000);
		assertThat(d.getRetriedTimesOnExecuteFailed()).isEqualTo(0);
		assertThat(d.getRetryOnNoQualified()).isEqualTo(5);
		assertThat(d.getRetryBackoffOnNoQualified()).isEqualTo(5000);
		assertThat(d.getRetriedTimesOnNoQualified()).isEqualTo(0);
		
		// uuid重复-----------------------------------------
//		assertThatExceptionOfType(SQLIntegrityConstraintException.class).isThrownBy(() -> delayJobService.create(dto));
	}
}

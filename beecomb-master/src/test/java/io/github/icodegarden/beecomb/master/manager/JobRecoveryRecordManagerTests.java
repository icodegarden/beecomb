package io.github.icodegarden.beecomb.master.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordQuery;
import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordQuery.With;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateOrUpdateJobRecoveryRecordDTO;
import io.github.icodegarden.beecomb.master.pojo.view.JobRecoveryRecordVO;
import io.github.icodegarden.commons.lang.util.SystemUtils;

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
	DelayJobManager delayJobManager;

	void create(Long jobId) {
		CreateOrUpdateJobRecoveryRecordDTO dto = new CreateOrUpdateJobRecoveryRecordDTO();
		dto.setDesc("ok");
		dto.setJobId(jobId);
		dto.setRecoveryAt(SystemUtils.now());
		dto.setSuccess(true);
		jobRecoveryRecordService.createOrUpdate(dto);
	}

	private ExecutableJobBO createJob() {
		CreateJobDTO dto = new CreateJobDTO();
		dto.setName("myjob");
		dto.setUuid(UUID.randomUUID().toString());
		dto.setType(JobType.Delay);
		dto.setExecutorName("n1");
		dto.setJobHandlerName("j1");

		CreateJobDTO.Delay delay = new CreateJobDTO.Delay();
		delay.setDelay(5000);
		delay.setRetryOnExecuteFailed(3);
		delay.setRetryBackoffOnExecuteFailed(3000);
		delay.setRetryOnNoQualified(5);
		delay.setRetryBackoffOnNoQualified(5000);
		dto.setDelay(delay);

		ExecutableJobBO job = delayJobManager.create(dto);
		return job;
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
		ExecutableJobBO job = createJob();

		create(job.getId());

		With with = JobRecoveryRecordQuery.With.WITH_MOST;
		JobRecoveryRecordVO findOne = jobRecoveryRecordService.findOne(job.getId(), with);
		assertThat(findOne).isNotNull();
	}

}

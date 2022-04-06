package io.github.icodegarden.beecomb.master.manager.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.DelayJobManager;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.DelayJobVO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class DelayJobManagerTests {

	@Autowired
	DelayJobManager delayJobManager;

	@Test
	void create() throws InterruptedException {
		Long jobId = 1L;

		CreateDelayJobDTO delay = new CreateDelayJobDTO();
		delay.setJobId(jobId);
		delay.setDelay(5000);
		delay.setRetryOnExecuteFailed(3);
		delay.setRetryBackoffOnExecuteFailed(3000);
		delay.setRetryOnNoQualified(5);
		delay.setRetryBackoffOnNoQualified(5000);

		delayJobManager.create(delay);

		DelayJobVO d = delayJobManager.findOne(jobId, null);

		assertThat(d).isNotNull();
		assertThat(d.getDelay()).isNotNull();
		assertThat(d.getRetryOnExecuteFailed()).isEqualTo(3);
		assertThat(d.getRetryBackoffOnExecuteFailed()).isEqualTo(3000);
		assertThat(d.getRetriedTimesOnExecuteFailed()).isEqualTo(0);
		assertThat(d.getRetryOnNoQualified()).isEqualTo(5);
		assertThat(d.getRetryBackoffOnNoQualified()).isEqualTo(5000);
		assertThat(d.getRetriedTimesOnNoQualified()).isEqualTo(0);

		// jobId重复-----------------------------------------
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> delayJobManager.create(delay));
	}
}

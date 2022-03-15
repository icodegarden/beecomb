package io.github.icodegarden.beecomb.master.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.mapper.DelayJobMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.DelayJobPO.Update;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class DelayJobMapperTests {

	@Autowired
	DelayJobMapper delayJobMapper;

	DelayJobPO create() {
		DelayJobPO delayJobPO = new DelayJobPO();
		delayJobPO.setDelay(5000);
		delayJobPO.setJobId(100L);
		delayJobPO.setRetryOnExecuteFailed(3);
		delayJobPO.setRetryBackoffOnExecuteFailed(3000);
		delayJobPO.setRetriedTimesOnExecuteFailed(1);
		delayJobPO.setRetryOnNoQualified(5);
		delayJobPO.setRetryBackoffOnNoQualified(5000);
		delayJobPO.setRetriedTimesOnNoQualified(2);
		
		delayJobMapper.add(delayJobPO);
		return delayJobPO;
	}

	@Test
	void add() {
		DelayJobPO po = create();
		assertThat(po).isNotNull();
	}

	@Test
	void findOne() {
		DelayJobPO po = create();
		DelayJobPO findOne = delayJobMapper.findOne(po.getJobId());

		assertThat(findOne).isNotNull();
		assertThat(findOne.getJobId()).isEqualTo(po.getJobId());
		assertThat(findOne.getDelay()).isEqualTo(5000);
		assertThat(findOne.getRetryOnExecuteFailed()).isEqualTo(3);
		assertThat(findOne.getRetryBackoffOnExecuteFailed()).isEqualTo(3000);
		assertThat(findOne.getRetriedTimesOnExecuteFailed()).isEqualTo(1);
		assertThat(findOne.getRetryOnNoQualified()).isEqualTo(5);
		assertThat(findOne.getRetryBackoffOnNoQualified()).isEqualTo(5000);
		assertThat(findOne.getRetriedTimesOnNoQualified()).isEqualTo(2);
	}

	@Test
	void update() {
		DelayJobPO po = create();
		Update update = DelayJobPO.Update.builder().jobId(po.getJobId()).retryOnExecuteFailed(6).retryBackoffOnExecuteFailed(30000)
				.retriedTimesOnExecuteFailed(3).retryOnNoQualified(10).retryBackoffOnNoQualified(60000)
				.retriedTimesOnNoQualified(8).build();
		delayJobMapper.update(update);

		DelayJobPO findOne = delayJobMapper.findOne(po.getJobId());

		assertThat(findOne).isNotNull();
		assertThat(findOne.getJobId()).isEqualTo(po.getJobId());
		assertThat(findOne.getRetryOnExecuteFailed()).isEqualTo(6);
		assertThat(findOne.getRetryBackoffOnExecuteFailed()).isEqualTo(30000);
		assertThat(findOne.getRetriedTimesOnExecuteFailed()).isEqualTo(3);
		assertThat(findOne.getRetryOnNoQualified()).isEqualTo(10);
		assertThat(findOne.getRetryBackoffOnNoQualified()).isEqualTo(60000);
		assertThat(findOne.getRetriedTimesOnNoQualified()).isEqualTo(8);
		
	}
}

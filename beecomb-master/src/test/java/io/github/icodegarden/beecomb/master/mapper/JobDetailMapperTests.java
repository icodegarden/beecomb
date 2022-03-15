package io.github.icodegarden.beecomb.master.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.mapper.JobDetailMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobDetailPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobDetailPO.Update;
/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class JobDetailMapperTests {

	@Autowired
	JobDetailMapper jobDetailMapper;

	JobDetailPO create() {
		JobDetailPO jobDetailPO = new JobDetailPO();
		jobDetailPO.setDesc("descdesc");
		jobDetailPO.setJobId(100L);
		jobDetailPO.setParams("[...]");
		
		jobDetailMapper.add(jobDetailPO);
		return jobDetailPO;
	}

	@Test
	void add() {
		JobDetailPO po = create();
		assertThat(po).isNotNull();
	}

	@Test
	void findOne() {
		JobDetailPO po = create();
		JobDetailPO findOne = jobDetailMapper.findOne(po.getJobId());

		assertThat(findOne).isNotNull();
		assertThat(findOne.getJobId()).isEqualTo(po.getJobId());
		assertThat(findOne.getDesc()).isEqualTo("descdesc");
		assertThat(findOne.getParams()).isEqualTo("[...]");
	}
	
	@Test
	void update() {
		JobDetailPO po = create();
		Update update = JobDetailPO.Update.builder().jobId(po.getJobId()).params("newparams").desc("newdesc").build();
		jobDetailMapper.update(update);
		
		JobDetailPO findOne = jobDetailMapper.findOne(po.getJobId());

		assertThat(findOne).isNotNull();
		assertThat(findOne.getJobId()).isEqualTo(po.getJobId());
		assertThat(findOne.getDesc()).isEqualTo("newdesc");
		assertThat(findOne.getParams()).isEqualTo("newparams");
	}
}

package io.github.icodegarden.beecomb.master.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.master.pojo.data.JobRecoveryRecordDO;
import io.github.icodegarden.beecomb.master.pojo.persistence.JobRecoveryRecordPO;
import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class JobRecoveryRecordMapperTests {

	@Autowired
	JobRecoveryRecordMapper jobRecoveryRecordMapper;

	JobRecoveryRecordPO buildPO() {
		JobRecoveryRecordPO po = new JobRecoveryRecordPO();
		po.setDesc("desc");
		po.setJobId(1L);
		po.setRecoveryAt(LocalDateTime.now());
		po.setSuccess(true);
		
		return po;
	}
	
	@Test
	void addOrUpdate() {
		JobRecoveryRecordPO po = buildPO();
		
		jobRecoveryRecordMapper.addOrUpdate(po);//第一次是新增
		jobRecoveryRecordMapper.addOrUpdate(po);//后面是更新
		jobRecoveryRecordMapper.addOrUpdate(po);//后面是更新
		
		JobRecoveryRecordQuery query = JobRecoveryRecordQuery.builder().build();
		List<JobRecoveryRecordDO> all = jobRecoveryRecordMapper.findAll(query);
		
		assertThat(all.size()).isEqualTo(1);
	}

	@Test
	void findOne() {
		JobRecoveryRecordPO po = buildPO();
		jobRecoveryRecordMapper.addOrUpdate(po);//第一次是新增
		
		JobRecoveryRecordDO findOne = jobRecoveryRecordMapper.findOne(po.getJobId(), null);
		
		assertThat(findOne.getJobId()).isEqualTo(po.getJobId());
	}
}


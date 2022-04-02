package io.github.icodegarden.beecomb.master.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.mapper.DelayJobMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO.Delay;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service("delayJobService")
public class DelayJobManager extends AbstractJobManager {

	@Autowired
	private DelayJobMapper delayJobMapper;
	
	@Transactional
	@Override
	public ExecutableJobBO create(CreateJobDTO dto) throws IllegalArgumentException {
		Delay delay = dto.getDelay();
		if (delay == null) {
			throw new IllegalArgumentException("delay must not null");
		}
		if(delay.getDelay() == null) {
			throw new IllegalArgumentException("delay.delay must not null");
		}
		JobMainPO main = createBase(dto);

		DelayJobPO delayJobPO = new DelayJobPO();
		delayJobPO.setDelay(delay.getDelay());
		delayJobPO.setJobId(main.getId());
		delayJobPO.setRetryOnExecuteFailed(delay.getRetryOnExecuteFailed());
		delayJobPO.setRetryBackoffOnExecuteFailed(delay.getRetryBackoffOnExecuteFailed());
		delayJobPO.setRetryOnNoQualified(delay.getRetryOnNoQualified());
		delayJobPO.setRetryBackoffOnNoQualified(delay.getRetryBackoffOnNoQualified());
		
		delayJobMapper.add(delayJobPO);
		
		return findOneExecutableJob(main.getId());
	}

}

package io.github.icodegarden.beecomb.common.db.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.db.mapper.DelayJobMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.db.pojo.transfer.CreateDelayJobDTO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class DelayJobManager {

	@Autowired
	private DelayJobMapper delayJobMapper;

	public void create(CreateDelayJobDTO dto) {
		Assert.notNull(dto.getDelay(), "delay must not null");

		DelayJobPO po = new DelayJobPO();
		BeanUtils.copyProperties(dto, po);

		delayJobMapper.add(po);
	}

}

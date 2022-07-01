package io.github.icodegarden.beecomb.common.backend.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.backend.mapper.DelayJobMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.data.DelayJobDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.DelayJobPO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.DelayJobQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.DelayJobVO;
import io.github.icodegarden.commons.springboot.exception.SQLConstraintException;

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
		dto.validate();

		DelayJobPO po = new DelayJobPO();
		BeanUtils.copyProperties(dto, po);

		try {
			delayJobMapper.add(po);
		} catch (DataIntegrityViolationException e) {
			throw new SQLConstraintException(e);
		}
	}

	public DelayJobVO findOne(Long jobId, @Nullable DelayJobQuery.With with) {
		DelayJobDO one = delayJobMapper.findOne(jobId, with);
		return DelayJobVO.of(one);
	}
	
	public boolean delete(Long jobId) {
		return delayJobMapper.delete(jobId) == 1;
	}
}

package io.github.icodegarden.beecomb.common.db.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.db.exception.SQLIntegrityConstraintException;
import io.github.icodegarden.beecomb.common.db.mapper.JobDetailMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobDetailPO;
import io.github.icodegarden.beecomb.common.db.pojo.transfer.CreateJobDetailDTO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobDetailManager {

	@Autowired
	private JobDetailMapper jobDetailMapper;

	public void create(CreateJobDetailDTO dto) throws SQLIntegrityConstraintException {
		JobDetailPO po = new JobDetailPO();
		BeanUtils.copyProperties(dto, po);

		try {
			jobDetailMapper.add(po);
		} catch (DataIntegrityViolationException e) {
			throw new SQLIntegrityConstraintException(e);
		}
	}

}

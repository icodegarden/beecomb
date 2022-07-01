package io.github.icodegarden.beecomb.common.backend.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.backend.mapper.JobDetailMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.data.JobDetailDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobDetailPO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobDetailQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobDetailDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobDetailDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobDetailVO;
import io.github.icodegarden.commons.springboot.exception.SQLConstraintException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobDetailManager {

	@Autowired
	private JobDetailMapper jobDetailMapper;

	public void create(CreateJobDetailDTO dto) {
		dto.validate();

		JobDetailPO po = new JobDetailPO();
		BeanUtils.copyProperties(dto, po);

		try {
			jobDetailMapper.add(po);
		} catch (DataIntegrityViolationException e) {
			throw new SQLConstraintException(e);
		}
	}

	public JobDetailVO findOne(Long jobId, @Nullable JobDetailQuery.With with) {
		JobDetailDO one = jobDetailMapper.findOne(jobId, with);
		return JobDetailVO.of(one);
	}

	public boolean update(UpdateJobDetailDTO dto) {
		JobDetailPO.Update update = new JobDetailPO.Update();
		BeanUtils.copyProperties(dto, update);

		return jobDetailMapper.update(update) == 1;
	}

	public boolean delete(Long jobId) {
		return jobDetailMapper.delete(jobId) == 1;
	}
}

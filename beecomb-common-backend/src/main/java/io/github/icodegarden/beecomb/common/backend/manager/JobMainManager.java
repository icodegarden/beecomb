package io.github.icodegarden.beecomb.common.backend.manager;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.icodegarden.beecomb.common.backend.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.data.JobMainDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobMainPO.Update;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobMainDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobMainEnQueueDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobMainOnExecutedDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.backend.util.PageHelperUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.springboot.exception.SQLConstraintException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobMainManager {

	@Autowired
	private JobMainMapper jobMainMapper;

	public void create(CreateJobMainDTO dto) {
		dto.validate();

		JobMainPO po = new JobMainPO();
		BeanUtils.copyProperties(dto, po);

//		po.setCreatedBy(SecurityUtils.getUsername());
		po.setCreatedAt(SystemUtils.now());

		try {
			jobMainMapper.add(po);
			dto.setId(po.getId());
		} catch (DataIntegrityViolationException e) {
			throw new SQLConstraintException(e);
		}
	}

	public Page<JobMainVO> page(JobMainQuery query) {
		PageHelper.startPage(query.getPage(), query.getSize());

		Page<JobMainDO> page = (Page<JobMainDO>) jobMainMapper.findAll(query);

		Page<JobMainVO> p = PageHelperUtils.ofPage(page, jobDO -> JobMainVO.of(jobDO));
		return p;
	}

	public List<JobMainVO> list(JobMainQuery query) {
		query.setLimitDefaultValueIfNotPresent();

		List<JobMainDO> list = jobMainMapper.findAll(query);

		return list.stream().map(jobDO -> JobMainVO.of(jobDO)).collect(Collectors.toList());
	}

	public JobMainVO findOne(Long id, @Nullable JobMainQuery.With with) {
		JobMainDO one = jobMainMapper.findOne(id, with);
		return JobMainVO.of(one);
	}

	public JobMainVO findByUUID(String uuid, @Nullable JobMainQuery.With with) {
		JobMainDO jobDO = jobMainMapper.findByUUID(uuid, with);
		return JobMainVO.of(jobDO);
	}

	public boolean updateOnExecuted(UpdateJobMainOnExecutedDTO dto) {
		Update update = new JobMainPO.Update();
		BeanUtils.copyProperties(dto, update);

		return jobMainMapper.update(update) == 1;
	}

	public boolean updateEnQueue(UpdateJobMainEnQueueDTO dto) {
		dto.validate();

		Update update = new JobMainPO.Update();
		BeanUtils.copyProperties(dto, update);

		/**
		 * 设为已队列
		 */
		update.setQueued(true);
		update.setQueuedAt(SystemUtils.now());

		return jobMainMapper.update(update) == 1;
	}
}
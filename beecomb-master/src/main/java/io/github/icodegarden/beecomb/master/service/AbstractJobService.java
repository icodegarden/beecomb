package io.github.icodegarden.beecomb.master.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.icodegarden.beecomb.common.db.exception.SQLIntegrityConstraintException;
import io.github.icodegarden.beecomb.common.db.mapper.JobDetailMapper;
import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.db.pojo.data.JobDO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobDetailPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobQuery;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.view.JobVO;
import io.github.icodegarden.beecomb.master.util.PageHelperUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AbstractJobService implements JobService {

	@Autowired
	protected JobMainMapper jobMainMapper;
	@Autowired
	protected JobDetailMapper jobDetailMapper;

	/**
	 * 
	 * @param dto
	 * @return 只包含业务必须字段值
	 */
	protected JobMainPO createBase(CreateJobDTO dto) throws SQLIntegrityConstraintException {
		JobMainPO jobMainPO = new JobMainPO();
		jobMainPO.setExecuteTimeout(dto.getExecuteTimeout());
		jobMainPO.setName(dto.getName());
		jobMainPO.setPriority(dto.getPriority());
		jobMainPO.setType(dto.getType());
		jobMainPO.setUuid(dto.getUuid());
		jobMainPO.setWeight(dto.getWeight());
		jobMainPO.setExecutorName(dto.getExecutorName());
		jobMainPO.setJobHandlerName(dto.getJobHandlerName());
		jobMainPO.setCreatedBy(SecurityUtils.getUsername());
		jobMainPO.setCreatedAt(SystemUtils.now());

		try {
			jobMainMapper.add(jobMainPO);
		} catch (DataIntegrityViolationException e) {
			throw new SQLIntegrityConstraintException(e);
		}

		JobDetailPO jobDetailPO = new JobDetailPO();
		jobDetailPO.setJobId(jobMainPO.getId());
		jobDetailPO.setDesc(dto.getDesc());
		jobDetailPO.setParams(dto.getParams());
		try {
			jobDetailMapper.add(jobDetailPO);
		} catch (DataIntegrityViolationException e) {
			throw new SQLIntegrityConstraintException(e);
		}

		return jobMainPO;
	}

	@Override
	public ExecutableJobBO findOneExecutableJob(Long id) {
		JobDO jobDO = jobMainMapper.findOne(id, JobQuery.With.WITH_EXECUTABLE);
		return jobDO.toExecutableJobBO();
	}

	public Page<JobVO> page(JobQuery query) {
		PageHelper.startPage(query.getPage(), query.getSize());

		Page<JobDO> page = (Page<JobDO>) jobMainMapper.findAll(query);

		Page<JobVO> p = PageHelperUtils.ofPage(page, jobDO -> JobVO.of(jobDO));
		return p;
	}

	public JobVO findOne(Long id, JobQuery.With with) {
		JobDO jobDO = jobMainMapper.findOne(id, with);
		return JobVO.of(jobDO);
	}

	public JobVO findByUUID(String uuid, JobQuery.With with) {
		JobDO jobDO = jobMainMapper.findByUUID(uuid, with);
		return JobVO.of(jobDO);
	}
}

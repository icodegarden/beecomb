package io.github.icodegarden.beecomb.master.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.db.pojo.data.JobDO;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobQuery;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobWith;
import io.github.icodegarden.beecomb.master.pojo.view.JobVO;
import io.github.icodegarden.beecomb.master.util.PageHelperUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobService {

	@Autowired
	private JobMainMapper jobMainMapper;
//	@Autowired
//	private JobDetailMapper jobDetailMapper;

	public Page<JobVO> page(JobQuery query) {
		PageHelper.startPage(query.getPage(), query.getSize());

		Page<JobDO> page = (Page<JobDO>) jobMainMapper.findAll(query);

		Page<JobVO> p = PageHelperUtils.ofPage(page, jobDO -> doConvertVo(jobDO));
		return p;
	}

	public JobVO findOne(Long id, JobWith with) {
		JobDO jobDO = jobMainMapper.findOne(id, with);
		return doConvertVo(jobDO);
	}

	public JobVO findByUUID(String uuid, JobWith with) {
		JobDO jobDO = jobMainMapper.findByUUID(uuid, with);
		return doConvertVo(jobDO);
	}

	private JobVO doConvertVo(JobDO jobDO) {
		JobVO jobVO = new JobVO();
		BeanUtils.copyProperties(jobDO.getJobMain(), jobVO);
		if (jobDO.getJobDetail() != null) {
			BeanUtils.copyProperties(jobDO.getJobDetail(), jobVO);
		}
		if (jobDO.getDelayJob() != null) {
			BeanUtils.copyProperties(jobDO.getDelayJob(), jobVO);
		}
		if (jobDO.getScheduleJob() != null) {
			BeanUtils.copyProperties(jobDO.getScheduleJob(), jobVO);
		}
		return jobVO;
	}
}

package io.github.icodegarden.beecomb.master.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.icodegarden.beecomb.common.db.exception.SQLIntegrityConstraintException;
import io.github.icodegarden.beecomb.master.mapper.JobRecoveryRecordMapper;
import io.github.icodegarden.beecomb.master.pojo.data.JobRecoveryRecordDO;
import io.github.icodegarden.beecomb.master.pojo.persistence.JobRecoveryRecordPO;
import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordQuery;
import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordWith;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateOrUpdateJobRecoveryRecordDTO;
import io.github.icodegarden.beecomb.master.pojo.view.JobRecoveryRecordVO;
import io.github.icodegarden.beecomb.master.pojo.view.JobVO;
import io.github.icodegarden.beecomb.master.util.PageHelperUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobRecoveryRecordService {

	@Autowired
	private JobRecoveryRecordMapper jobRecoveryRecordMapper;

	public JobRecoveryRecordPO createOrUpdate(CreateOrUpdateJobRecoveryRecordDTO dto) {
		JobRecoveryRecordPO po = new JobRecoveryRecordPO();
		BeanUtils.copyProperties(dto, po);

		try {
			jobRecoveryRecordMapper.addOrUpdate(po);
			return po;
		} catch (DataIntegrityViolationException e) {
			throw new SQLIntegrityConstraintException(e);
		}
	}

	public Page<JobRecoveryRecordVO> page(JobRecoveryRecordQuery query) {
		PageHelper.startPage(query.getPage(), query.getSize());

		Page<JobRecoveryRecordDO> page = (Page<JobRecoveryRecordDO>) jobRecoveryRecordMapper.findAll(query);

		Page<JobRecoveryRecordVO> p = PageHelperUtils.ofPage(page, _do -> doConvertVo(_do));
		return p;
	}

	public JobRecoveryRecordVO findOne(Long jobId, JobRecoveryRecordWith with) {
		JobRecoveryRecordDO _do = jobRecoveryRecordMapper.findOne(jobId, with);
		return doConvertVo(_do);
	}

	private JobRecoveryRecordVO doConvertVo(JobRecoveryRecordDO jobRecoveryRecordDO) {
		JobRecoveryRecordVO vo = new JobRecoveryRecordVO();
		BeanUtils.copyProperties(jobRecoveryRecordDO.getJobRecoveryRecord(), vo);
		if (jobRecoveryRecordDO.getJobMain() != null) {
			JobVO jobVO = new JobVO();
			BeanUtils.copyProperties(jobRecoveryRecordDO.getJobMain(), jobVO);
			
			vo.setJob(jobVO);
		}
		return vo;
	}
}

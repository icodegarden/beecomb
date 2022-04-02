package io.github.icodegarden.beecomb.master.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.icodegarden.beecomb.master.mapper.JobRecoveryRecordMapper;
import io.github.icodegarden.beecomb.master.pojo.data.JobRecoveryRecordDO;
import io.github.icodegarden.beecomb.master.pojo.persistence.JobRecoveryRecordPO;
import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordQuery;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateOrUpdateJobRecoveryRecordDTO;
import io.github.icodegarden.beecomb.master.pojo.view.JobRecoveryRecordVO;
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

	/**
	 * 每个job最多存在一条记录
	 * 
	 * @param dto
	 * @return
	 */
	public void createOrUpdate(CreateOrUpdateJobRecoveryRecordDTO dto) {
		JobRecoveryRecordPO po = new JobRecoveryRecordPO();
		BeanUtils.copyProperties(dto, po);

		jobRecoveryRecordMapper.addOrUpdate(po);
	}

	public Page<JobRecoveryRecordVO> page(JobRecoveryRecordQuery query) {
		PageHelper.startPage(query.getPage(), query.getSize());

		Page<JobRecoveryRecordDO> page = (Page<JobRecoveryRecordDO>) jobRecoveryRecordMapper.findAll(query);

		Page<JobRecoveryRecordVO> p = PageHelperUtils.ofPage(page, _do -> JobRecoveryRecordVO.of(_do));
		return p;
	}

	public JobRecoveryRecordVO findOne(Long jobId, JobRecoveryRecordQuery.With with) {
		JobRecoveryRecordDO _do = jobRecoveryRecordMapper.findOne(jobId, with);
		return JobRecoveryRecordVO.of(_do);
	}
}

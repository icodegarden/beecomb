package io.github.icodegarden.beecomb.common.backend.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.icodegarden.beecomb.common.backend.mapper.JobExecuteRecordMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.data.JobExecuteRecordDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobExecuteRecordPO;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobExecuteRecordQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobExecuteRecordDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobMainOnExecutedDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobExecuteRecordVO;
import io.github.icodegarden.beecomb.common.backend.util.PageHelperUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobExecuteRecordManager {

	@Autowired
	private JobExecuteRecordMapper jobExecuteRecordMapper;

	public void create(CreateJobExecuteRecordDTO dto) {
		JobExecuteRecordPO po = new JobExecuteRecordPO();
		BeanUtils.copyProperties(dto, po);

		jobExecuteRecordMapper.add(po);
	}

	public void createOnExecuted(UpdateJobMainOnExecutedDTO update) {
		CreateJobExecuteRecordDTO createJobExecuteRecordDTO = new CreateJobExecuteRecordDTO();
		createJobExecuteRecordDTO.setExecuteExecutor(update.getLastExecuteExecutor());
		createJobExecuteRecordDTO.setExecuteReturns(update.getLastExecuteReturns());
		createJobExecuteRecordDTO.setJobId(update.getId());
		createJobExecuteRecordDTO.setSuccess(update.getLastExecuteSuccess());
		createJobExecuteRecordDTO.setTrigAt(update.getLastTrigAt());
		createJobExecuteRecordDTO.setTrigResult(update.getLastTrigResult());

		create(createJobExecuteRecordDTO);
	}

	public Page<JobExecuteRecordVO> page(JobExecuteRecordQuery query) {
		PageHelper.startPage(query.getPage(), query.getSize());

		Page<JobExecuteRecordDO> page = (Page<JobExecuteRecordDO>) jobExecuteRecordMapper.findAll(query);

		Page<JobExecuteRecordVO> p = PageHelperUtils.ofPage(page, one -> JobExecuteRecordVO.of(one));
		return p;
	}

	public JobExecuteRecordVO findOne(Long id, JobExecuteRecordQuery.With with) {
		JobExecuteRecordDO one = jobExecuteRecordMapper.findOne(id, with);
		return JobExecuteRecordVO.of(one);
	}
}

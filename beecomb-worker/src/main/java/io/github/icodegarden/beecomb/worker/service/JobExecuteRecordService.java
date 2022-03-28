package io.github.icodegarden.beecomb.worker.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.db.mapper.JobExecuteRecordMapper;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobExecuteRecordPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.CreateJobExecuteRecordDTO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobExecuteRecordService {

	@Autowired
	private JobExecuteRecordMapper jobExecuteRecordMapper;

	public void create(CreateJobExecuteRecordDTO dto) {
		JobExecuteRecordPO po = new JobExecuteRecordPO();
		BeanUtils.copyProperties(dto, po);

		jobExecuteRecordMapper.add(po);
	}

	public void createOnJobUpdate(JobMainPO.Update update) {
		CreateJobExecuteRecordDTO createJobExecuteRecordDTO = new CreateJobExecuteRecordDTO();
		createJobExecuteRecordDTO.setExecuteExecutor(update.getLastExecuteExecutor());
		createJobExecuteRecordDTO.setExecuteReturns(update.getLastExecuteReturns());
		createJobExecuteRecordDTO.setJobId(update.getId());
		createJobExecuteRecordDTO.setSuccess(update.getLastExecuteSuccess());
		createJobExecuteRecordDTO.setTrigAt(update.getLastTrigAt());
		createJobExecuteRecordDTO.setTrigResult(update.getLastTrigResult());
		
		create(createJobExecuteRecordDTO);
	}
}

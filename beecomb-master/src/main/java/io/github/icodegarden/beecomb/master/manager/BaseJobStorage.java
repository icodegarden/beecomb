package io.github.icodegarden.beecomb.master.manager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.icodegarden.beecomb.common.db.exception.SQLIntegrityConstraintException;
import io.github.icodegarden.beecomb.common.db.mapper.JobDetailMapper;
import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.db.pojo.data.JobDO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobDetailPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobQuery;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobWith;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobWith.DelayJob;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobWith.JobDetail;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobWith.JobMain;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobWith.ScheduleJob;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class BaseJobStorage implements JobStorage {

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
		JobDO jobDO = jobMainMapper.findOne(id, executableJobWith());
		return jobDO.toExecutableJobBO();
	}

	@Override
	public boolean hasNoQueuedActually(LocalDateTime nextTrigAtLt) {
		JobQuery query = JobQuery.builder().nextTrigAtLt(nextTrigAtLt).limit("limit 1").build();
		List<JobDO> dos = jobMainMapper.findAll(query);
		return dos.size() >= 1;
	}

	@Override
	public int recoveryThatNoQueuedActually(LocalDateTime nextTrigAtLt) {
		return jobMainMapper.updateToNoQueued(nextTrigAtLt);
	}

	/**
	 * 未end、未queued、按priority优先级
	 */
	@Override
	public List<ExecutableJobBO> listJobsShouldRecovery(int skip, int size) {
		JobQuery query = JobQuery.builder().end(false).queued(false).with(executableJobWith())
				.sort("order by a.priority desc").limit("limit " + skip + "," + size).build();

		List<JobDO> dos = jobMainMapper.findAll(query);
		if (dos.isEmpty()) {
			return Collections.emptyList();
		}
		return dos.stream().map(JobDO::toExecutableJobBO).collect(Collectors.toList());
	}

	protected JobWith executableJobWith() {
		return JobWith.builder()
				.jobMain(JobMain.builder().createdAt(true).lastExecuteExecutor(true).lastExecuteReturns(true)
						.lastTrigResult(true).build())
				.jobDetail(JobDetail.builder().params(true).build()).delayJob(DelayJob.builder().build())
				.scheduleJob(ScheduleJob.builder().build()).build();

	}
}

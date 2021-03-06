package io.github.icodegarden.beecomb.master.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.DelayJobManager;
import io.github.icodegarden.beecomb.common.backend.manager.JobDetailManager;
import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.manager.PendingRecoveryJobManager;
import io.github.icodegarden.beecomb.common.backend.manager.ScheduleJobManager;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.query.PendingRecoveryJobQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobDetailDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobMainDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreatePendingRecoveryJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateScheduleJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateDelayJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobDetailDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobMainDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateScheduleJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.PendingRecoveryJobVO;
import io.github.icodegarden.beecomb.common.backend.service.AbstractBackendJobService;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.UpdateJobOpenapiDTO;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobFacadeManager extends AbstractBackendJobService {

	@Autowired
	private JobMainManager jobMainManager;
	@Autowired
	private JobDetailManager jobDetailManager;
	@Autowired
	private DelayJobManager delayJobManager;
	@Autowired
	private ScheduleJobManager scheduleJobManager;
	@Autowired
	private PendingRecoveryJobManager pendingRecoveryJobManager;

	@Transactional
	public ExecutableJobBO create(CreateJobDTO dto) {
		CreateJobMainDTO createJobMainDTO = new CreateJobMainDTO();
		BeanUtils.copyProperties(dto, createJobMainDTO);
		createJobMainDTO.setCreatedBy(SecurityUtils.getUsername());// IMPT
		jobMainManager.create(createJobMainDTO);

		CreateJobDetailDTO createJobDetailDTO = new CreateJobDetailDTO();
		BeanUtils.copyProperties(dto, createJobDetailDTO);
		createJobDetailDTO.setJobId(createJobMainDTO.getId());
		jobDetailManager.create(createJobDetailDTO);

		if (dto.getType() == JobType.Delay) {
			CreateDelayJobDTO createDelayJobDTO = new CreateDelayJobDTO();
			BeanUtils.copyProperties(dto.getDelay(), createDelayJobDTO);
			createDelayJobDTO.setJobId(createJobMainDTO.getId());
			delayJobManager.create(createDelayJobDTO);
		}
		if (dto.getType() == JobType.Schedule) {
			CreateScheduleJobDTO createScheduleJobDTO = new CreateScheduleJobDTO();
			BeanUtils.copyProperties(dto.getSchedule(), createScheduleJobDTO);
			createScheduleJobDTO.setJobId(createJobMainDTO.getId());
			scheduleJobManager.create(createScheduleJobDTO);
		}

		ExecutableJobBO job = findOneExecutableJob(createJobMainDTO.getId());
		/**
		 * DTO???priority???????????????null????????????job
		 */
		createPendingRecoveryJob(job);

		return job;
	}

	private void createPendingRecoveryJob(ExecutableJobBO job) {
		CreatePendingRecoveryJobDTO createPendingRecoveryJobDTO = new CreatePendingRecoveryJobDTO();
		createPendingRecoveryJobDTO.setJobId(job.getId());
		createPendingRecoveryJobDTO.setPriority(job.getPriority());
		pendingRecoveryJobManager.create(createPendingRecoveryJobDTO);
	}

	@Transactional
	public boolean update(UpdateJobOpenapiDTO dto) {
		return update(dto, false);
	}

	/**
	 * 
	 * @param dto
	 * @param removedQueue ???????????????????????????????????????????????????nextTrigAt???false
	 * @return
	 */
	@Transactional
	public boolean update(UpdateJobOpenapiDTO dto, boolean removedQueue) {
		dto.validate();

		boolean update = false;

		UpdateJobMainDTO updateJobMainDTO = new UpdateJobMainDTO();
		BeanUtils.copyProperties(dto, updateJobMainDTO);
		if (removedQueue) {
			updateJobMainDTO.setNextTrigAtNull(true);
		}
		if (updateJobMainDTO.shouldUpdate()) {
			update = jobMainManager.update(updateJobMainDTO);
		}

		UpdateJobDetailDTO updateJobDetailDTO = new UpdateJobDetailDTO();
		BeanUtils.copyProperties(dto, updateJobDetailDTO);
		updateJobDetailDTO.setJobId(dto.getId());
		if (updateJobDetailDTO.shouldUpdate()) {
			update = jobDetailManager.update(updateJobDetailDTO);
		}

		if (dto.getDelay() != null) {
			UpdateDelayJobDTO updateDelayJobDTO = new UpdateDelayJobDTO();
			BeanUtils.copyProperties(dto.getDelay(), updateDelayJobDTO);
			updateDelayJobDTO.setJobId(dto.getId());
			if (updateDelayJobDTO.shouldUpdate()) {
				update = delayJobManager.update(updateDelayJobDTO);
			}
		}

		if (dto.getSchedule() != null) {
			UpdateScheduleJobDTO updateScheduleJobDTO = new UpdateScheduleJobDTO();
			BeanUtils.copyProperties(dto.getSchedule(), updateScheduleJobDTO);
			updateScheduleJobDTO.setJobId(dto.getId());
			if (updateScheduleJobDTO.shouldUpdate()) {
				update = scheduleJobManager.update(updateScheduleJobDTO);
			}
		}

		return update;
	}

	@Transactional
	public void delete(Long jobId) {
		boolean delete = jobMainManager.delete(jobId);
		if (delete) {
			jobDetailManager.delete(jobId);
			delayJobManager.delete(jobId);
			scheduleJobManager.delete(jobId);
		}
	}

	/**
	 * ??????????????????????????????????????????
	 * 
	 * @param nextTrigAtLt
	 * @return
	 */
	public boolean hasNoQueuedActually(LocalDateTime nextTrigAtLt) {
		JobMainQuery query = JobMainQuery.builder().nextTrigAtLt(nextTrigAtLt).end(false).limit("limit 1").build();
		List<JobMainVO> vos = jobMainManager.list(query);
		return vos.size() >= 1;
	}

	/**
	 * ??????nextTrigAt?????????????????????->???????????????<br>
	 * ????????????
	 * 
	 * @param nextTrigAtLt
	 * @return
	 */
	public int recoveryThatNoQueuedActually(LocalDateTime nextTrigAtLt) {
		int count = pendingRecoveryJobManager.insertSelectByScan(nextTrigAtLt);
		if (count > 0) {
			return jobMainManager.updateToNoQueuedByScan(nextTrigAtLt);
		}
		return 0;
	}

	/**
	 * ???????????????????????????????????????->???????????????<br>
	 * ????????????
	 * 
	 * @param queuedAtInstance
	 * @return
	 */
	public int recoveryThatNoQueuedActuallyByQueuedAtInstance(String queuedAtInstance) {
		int count = pendingRecoveryJobManager.insertSelectByInstance(queuedAtInstance);
		if (count > 0) {
			return jobMainManager.updateToNoQueuedByInstance(queuedAtInstance);
		}
		return 0;
	}

	/**
	 * ????????????????????????<br>
	 * 
	 * @param skip
	 * @param size
	 * @return
	 */
	public List<ExecutableJobBO> listJobsShouldRecovery(int skip, int size) {
		PendingRecoveryJobQuery query = PendingRecoveryJobQuery.builder().sort("order by a.priority desc")
				.limit("limit " + skip + "," + size).build();
		List<PendingRecoveryJobVO> list = pendingRecoveryJobManager.list(query);

		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		List<Long> jobIds = list.stream().map(PendingRecoveryJobVO::getJobId).collect(Collectors.toList());

//		JobMainQuery query = JobMainQuery.builder().end(false).queued(false).with(JobMainQuery.With.WITH_EXECUTABLE)
//				.sort("order by a.priority desc").limit("limit " + skip + "," + size).build();

		JobMainQuery jobQuery = JobMainQuery.builder().jobIds(jobIds).sort("order by a.priority desc")//????????????????????????????????????????????????
				.with(JobMainQuery.With.WITH_EXECUTABLE).build();
		List<JobMainVO> vos = jobMainManager.list(jobQuery);
		if (vos.isEmpty()) {
			return Collections.emptyList();
		}
		return vos.stream().map(JobMainVO::toExecutableJob).collect(Collectors.toList());
	}

}

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
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdateJobDTO;
import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
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
		 * DTO的priority字段可能是null所以先查job
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

//	@Transactional
//	public boolean update(UpdateJobOpenapiDTO dto) {
//		return update(dto, false);
//	}

	/**
	 * 
	 * @param dto
	 * @param removedQueue 是否引起了移除队列，是则会自动更新nextTrigAt为false
	 * @return
	 */
	@Transactional
	public boolean update(UpdateJobDTO dto, boolean removedQueue) {
		dto.validate();
		UpdateJobMainDTO updateJobMainDTO = new UpdateJobMainDTO();
		BeanUtils.copyProperties(dto, updateJobMainDTO);
		
		boolean update = false;
		if (removedQueue) {
			/**
			 * 任务被removedQueue时需要更新为null，因为关系到重新进队列的时间计算
			 */
			updateJobMainDTO.setNextTrigAtNull(true);
			updateJobMainDTO.setQueued(!removedQueue);
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
	 * 识别是否存在已不在队列的任务
	 * 
	 * @param nextTrigAtLt
	 * @return
	 */
	public boolean hasNoQueuedActually(LocalDateTime nextTrigAtLt) {
		JobMainQuery query = JobMainQuery.builder().nextTrigAtLt(nextTrigAtLt).end(false).size(1).build();
		List<JobMainVO> vos = jobMainManager.list(query);
		return !vos.isEmpty();
	}

	/**
	 * 更新nextTrigAt超过给定的时间->状态未队列<br>
	 * 不加事务
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
	 * 更新所在实例实际已不存在的->状态未队列<br>
	 * 不加事务
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
	 * 获取待恢复的任务<br>
	 * 
	 * @param skip
	 * @param size
	 * @return
	 */
	public List<ExecutableJobBO> listJobsShouldRecovery(int skip, int size) {
		List<PendingRecoveryJobVO> list = pendingRecoveryJobManager.listJobsShouldRecovery(skip, size);
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		List<Long> jobIds = list.stream().map(PendingRecoveryJobVO::getJobId).collect(Collectors.toList());

//		JobMainQuery query = JobMainQuery.builder().end(false).queued(false).with(JobMainQuery.With.WITH_EXECUTABLE)
//				.sort("order by a.priority desc").limit("limit " + skip + "," + size).build();

		JobMainQuery jobQuery = JobMainQuery.builder().jobIds(jobIds).orderBy("a.priority desc")// 此时排序已不重要，但也不影响性能
				.size(jobIds.size()).with(JobMainQuery.With.WITH_EXECUTABLE).build();
		List<JobMainVO> vos = jobMainManager.list(jobQuery);
		
		/**
		 * 若size不一样，则说明任务已不存在，删除pending多余部分
		 */
		if(vos.size() != list.size()) {
			List<Long> ids = vos.stream().map(JobMainVO::getId).collect(Collectors.toList());
			for(PendingRecoveryJobVO one:list) {
				if(!ids.contains(one.getJobId())) {
					log.info("delete PendingRecoveryJob because relation job not exist, jobId:{}", one.getJobId());
					pendingRecoveryJobManager.delete(one.getJobId());
				}
			}
		}
		
		if (vos.isEmpty()) {
			return Collections.emptyList();
		}
		return vos.stream().map(JobMainVO::toExecutableJob).collect(Collectors.toList());
	}

}

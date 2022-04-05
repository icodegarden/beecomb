package io.github.icodegarden.beecomb.master.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.db.manager.DelayJobManager;
import io.github.icodegarden.beecomb.common.db.manager.JobDetailManager;
import io.github.icodegarden.beecomb.common.db.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.db.manager.ScheduleJobManager;
import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.db.pojo.data.JobMainDO;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.db.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.common.db.pojo.transfer.CreateJobDetailDTO;
import io.github.icodegarden.beecomb.common.db.pojo.transfer.CreateJobMainDTO;
import io.github.icodegarden.beecomb.common.db.pojo.transfer.CreateScheduleJobDTO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.CreateJobOpenapiDTO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobService {

	@Autowired
	private JobMainMapper jobMainMapper;// FIXME 不应该直接出现
	@Autowired
	private JobMainManager jobMainManager;
	@Autowired
	private JobDetailManager jobDetailManager;
	@Autowired
	private DelayJobManager delayJbManager;
	@Autowired
	private ScheduleJobManager scheduleJobManager;

	@Transactional
	public ExecutableJobBO create(CreateJobOpenapiDTO dto) throws IllegalArgumentException {
		CreateJobMainDTO createJobMainDTO = new CreateJobMainDTO();
		BeanUtils.copyProperties(dto, createJobMainDTO);
		jobMainManager.create(createJobMainDTO);

		CreateJobDetailDTO createJobDetailDTO = new CreateJobDetailDTO();
		BeanUtils.copyProperties(dto, createJobDetailDTO);
		jobDetailManager.create(createJobDetailDTO);

		if (dto.getType() == JobType.Delay) {
			CreateDelayJobDTO createDelayJobDTO = new CreateDelayJobDTO();
			BeanUtils.copyProperties(dto.getDelay(), createDelayJobDTO);
			createDelayJobDTO.setJobId(createJobMainDTO.getId());
			delayJbManager.create(createDelayJobDTO);
		}
		if (dto.getType() == JobType.Schedule) {
			CreateScheduleJobDTO createScheduleJobDTO = new CreateScheduleJobDTO();
			BeanUtils.copyProperties(dto.getSchedule(), createScheduleJobDTO);
			createScheduleJobDTO.setJobId(createJobMainDTO.getId());
			scheduleJobManager.create(createScheduleJobDTO);
		}

		return findOneExecutableJob(createJobMainDTO.getId());
	}

	/**
	 * FIXME 返回体确定要这样吗
	 */
	public ExecutableJobBO findOneExecutableJob(Long id) {
		JobMainDO jobDO = jobMainMapper.findOne(id, JobMainQuery.With.WITH_EXECUTABLE);
		return jobDO.toExecutableJobBO();
	}

	public boolean hasNoQueuedActually(LocalDateTime nextTrigAtLt) {
		JobMainQuery query = JobMainQuery.builder().nextTrigAtLt(nextTrigAtLt).limit("limit 1").build();
		List<JobMainDO> dos = jobMainMapper.findAll(query);
		return dos.size() >= 1;
	}

	/**
	 * 更新nextTrigAt超过给定的时间->状态未队列
	 * 
	 * @param nextTrigAtLt
	 * @return
	 */
	public int recoveryThatNoQueuedActually(LocalDateTime nextTrigAtLt) {
		return jobMainMapper.updateToNoQueued(nextTrigAtLt);
	}

	/**
	 * 获取 未完成且未队列 的任务，这些任务应该被恢复<br>
	 * 未end、未queued、按priority优先级
	 * 
	 * @param skip
	 * @param size
	 * @return
	 */
	public List<ExecutableJobBO> listJobsShouldRecovery(int skip, int size) {
		JobMainQuery query = JobMainQuery.builder().end(false).queued(false).with(JobMainQuery.With.WITH_EXECUTABLE)
				.sort("order by a.priority desc").limit("limit " + skip + "," + size).build();

		List<JobMainDO> dos = jobMainMapper.findAll(query);
		if (dos.isEmpty()) {
			return Collections.emptyList();
		}
		return dos.stream().map(JobMainDO::toExecutableJobBO).collect(Collectors.toList());
	}

}

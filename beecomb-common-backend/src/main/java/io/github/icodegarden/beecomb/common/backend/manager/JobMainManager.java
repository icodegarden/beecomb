package io.github.icodegarden.beecomb.common.backend.manager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.icodegarden.beecomb.common.backend.constant.TableNameConstants;
import io.github.icodegarden.beecomb.common.backend.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.data.JobMainDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.JobMainPO.Update;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobMainCountQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreateJobMainDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobMainDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobMainEnQueueDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.UpdateJobMainOnExecutedDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainCountVO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.backend.util.PageHelperUtils;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.springboot.exception.SQLConstraintException;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobMainManager {

	@Autowired
	private JobMainMapper jobMainMapper;

	public void create(CreateJobMainDTO dto) {
		dto.validate();

		JobMainPO po = new JobMainPO();
		BeanUtils.copyProperties(dto, po);

		po.setCreatedBy(SecurityUtils.getUsername());
		po.setCreatedAt(SystemUtils.now());
		po.setUpdatedAt(SystemUtils.now());// ????????????

		try {
			jobMainMapper.add(po);
			dto.setId(po.getId());
		} catch (DataIntegrityViolationException e) {
			throw new SQLConstraintException(e);
		}
	}

	public Page<JobMainVO> page(JobMainQuery query) {
		boolean allowCount = false;
		if(query.getType() == null) {
			allowCount = PageHelperUtils.allowCount(TableNameConstants.JOB_MAIN);
		} else if(query.getType() == JobType.Delay) {
			allowCount = PageHelperUtils.allowCount(TableNameConstants.DELAY_JOB);
		} else if(query.getType() == JobType.Schedule) {
			allowCount = PageHelperUtils.allowCount(TableNameConstants.SCHEDULE_JOB);
		}
		
		PageHelper.startPage(query.getPage(), query.getSize(), allowCount);

		Page<JobMainDO> page = (Page<JobMainDO>) jobMainMapper.findAll(query);

		Page<JobMainVO> p = PageHelperUtils.ofPage(page, jobDO -> JobMainVO.of(jobDO));
		return p;
	}

	public List<JobMainVO> list(JobMainQuery query) {
		query.setLimitDefaultValueIfNotPresent();

		List<JobMainDO> list = jobMainMapper.findAll(query);

		return list.stream().map(jobDO -> JobMainVO.of(jobDO)).collect(Collectors.toList());
	}

	public JobMainVO findOne(Long id, @Nullable JobMainQuery.With with) {
		JobMainDO one = jobMainMapper.findOne(id, with);
		return JobMainVO.of(one);
	}

	public JobMainVO findByUUID(String uuid, @Nullable JobMainQuery.With with) {
		JobMainDO jobDO = jobMainMapper.findByUUID(uuid, with);
		return JobMainVO.of(jobDO);
	}

	public boolean update(UpdateJobMainDTO dto) {
		Update update = new JobMainPO.Update();
		BeanUtils.copyProperties(dto, update);
		update.setUpdatedBy(SecurityUtils.getUsername());
		update.setUpdatedAt(SystemUtils.now());

		return jobMainMapper.update(update) == 1;
	}

	/**
	 * ??????nextTrigAt?????????????????????->???????????????
	 * 
	 * @param nextTrigAtLt
	 * @return
	 */
	public int updateToNoQueuedByScan(LocalDateTime nextTrigAtLt) {
		return jobMainMapper.updateToNoQueuedByScan(nextTrigAtLt);
	}

	/**
	 * ???????????????????????????????????????->???????????????
	 * 
	 * @param queuedAtInstance
	 * @return
	 */
	public int updateToNoQueuedByInstance(String queuedAtInstance) {
		return jobMainMapper.updateToNoQueuedByInstance(queuedAtInstance);
	}

	public boolean updateOnExecuted(UpdateJobMainOnExecutedDTO dto) {
		Update update = new JobMainPO.Update();
		BeanUtils.copyProperties(dto, update);
		update.setUpdatedBy(SecurityUtils.getUsername());
		update.setUpdatedAt(SystemUtils.now());

		return jobMainMapper.update(update) == 1;
	}

	public boolean updateEnQueue(UpdateJobMainEnQueueDTO dto) {
		dto.validate();

		Update update = new JobMainPO.Update();
		BeanUtils.copyProperties(dto, update);
		update.setUpdatedBy(SecurityUtils.getUsername());
		update.setUpdatedAt(SystemUtils.now());

		/**
		 * ???????????????
		 */
		update.setQueued(true);
		update.setQueuedAt(SystemUtils.now());

		return jobMainMapper.update(update) == 1;
	}

	public boolean updateRemoveQueue(Long jobId) {
		Update update = new JobMainPO.Update();
		update.setUpdatedBy(SecurityUtils.getUsername());
		update.setUpdatedAt(SystemUtils.now());

		update.setQueued(false);
		update.setNextTrigAtNull(true);

		return jobMainMapper.update(update) == 1;
	}

	public boolean delete(Long id) {
		return jobMainMapper.delete(id) == 1;
	}

	/**
	 * EXPLAIN -- ?????? SELECT created_by, type, count( 0 ) AS count FROM job_main
	 * GROUP BY created_by, type
	 * 
	 * @return
	 */
	public List<JobMainCountVO> countTotalGroupByTypeAndCreateBy() {
		JobMainCountQuery query = JobMainCountQuery.builder()
				.groupBy(JobMainCountQuery.GroupBy.builder().createdBy(true).type(true).build()).build();
		return (List) jobMainMapper.count(query);
	}

	/**
	 * EXPLAIN -- ????????? SELECT created_by, type, count( 0 ) AS count FROM job_main
	 * where is_queued = 1 GROUP BY created_by, type
	 * 
	 * @return
	 */
	public List<JobMainCountVO> countQueuedGroupByTypeAndCreateBy() {
		JobMainCountQuery query = JobMainCountQuery.builder().queued(true)
				.groupBy(JobMainCountQuery.GroupBy.builder().createdBy(true).type(true).build()).build();
		return (List) jobMainMapper.count(query);
	}

	/**
	 * EXPLAIN -- ?????????????????? SELECT created_by, type, count( 0 ) AS count FROM job_main
	 * where is_queued = 0 and is_end = 0 GROUP BY created_by, type
	 * 
	 * @return
	 */
	public List<JobMainCountVO> countNoQueuedNoEndGroupByTypeAndCreateBy() {
		JobMainCountQuery query = JobMainCountQuery.builder().queued(false).end(false)
				.groupBy(JobMainCountQuery.GroupBy.builder().createdBy(true).type(true).build()).build();
		return (List) jobMainMapper.count(query);
	}

	/**
	 * EXPLAIN -- ?????????????????? SELECT created_by, type, count( 0 ) AS count FROM job_main
	 * where is_end = 1 and is_last_execute_success = 1 GROUP BY created_by, type
	 * 
	 * @return
	 */
	public List<JobMainCountVO> countEndLastExecuteSuccessGroupByTypeAndCreateBy() {
		JobMainCountQuery query = JobMainCountQuery.builder().end(true).lastExecuteSuccess(true)
				.groupBy(JobMainCountQuery.GroupBy.builder().createdBy(true).type(true).build()).build();
		return (List) jobMainMapper.count(query);
	}

	/**
	 * EXPLAIN -- ?????????????????? SELECT created_by, type, count( 0 ) AS count FROM job_main
	 * WHERE is_end = 1 AND is_last_execute_success = 1 GROUP BY created_by, type
	 * 
	 * @return
	 */
	public List<JobMainCountVO> countEndLastExecuteFailedGroupByTypeAndCreateBy() {
		JobMainCountQuery query = JobMainCountQuery.builder().end(true).lastExecuteSuccess(false)
				.groupBy(JobMainCountQuery.GroupBy.builder().createdBy(true).type(true).build()).build();
		return (List) jobMainMapper.count(query);
	}
}

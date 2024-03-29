package io.github.icodegarden.beecomb.common.backend.manager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
import io.github.icodegarden.beecomb.common.backend.util.TableDataCountUtils;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.nursery.springboot.exception.SQLConstraintException;
import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;
import io.github.icodegarden.nutrient.mybatis.util.PageHelperUtils;

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

		if (!StringUtils.hasText(po.getUuid())) {
			po.setUuid(io.github.icodegarden.nutrient.lang.util.StringUtils.uuid());
		}

		po.setCreatedBy(SecurityUtils.getUsername());
		po.setCreatedAt(SystemUtils.now());
		po.setUpdatedAt(po.getCreatedAt());// 防止时差

		try {
			jobMainMapper.add(po);
			dto.setId(po.getId());
		} catch (DataIntegrityViolationException e) {
			throw new SQLConstraintException(e);
		}
	}

	public Page<JobMainVO> page(JobMainQuery query) {
		boolean allowCount = false;
		if (query.getType() == null) {
			allowCount = TableDataCountUtils.allowCount(TableNameConstants.JOB_MAIN);
		} else if (query.getType() == JobType.Delay) {
			allowCount = TableDataCountUtils.allowCount(TableNameConstants.DELAY_JOB);
		} else if (query.getType() == JobType.Schedule) {
			allowCount = TableDataCountUtils.allowCount(TableNameConstants.SCHEDULE_JOB);
		}

		Page<Object> startPage = PageHelper.startPage(query.getPage(), query.getSize(), query.getOrderBy());
		startPage.setCount(allowCount);

		Page<JobMainDO> page = (Page<JobMainDO>) jobMainMapper.findAll(query);

		Page<JobMainVO> p = PageHelperUtils.ofPageNoCountAdapt(page, jobDO -> JobMainVO.of(jobDO));
		return p;
	}

	public List<JobMainVO> list(JobMainQuery query) {
		Page<Object> startPage = PageHelper.startPage(query.getPage(), query.getSize(), query.getOrderBy());
		startPage.setCount(false);

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

	/**
	 * 常规更新字段
	 */
	public boolean update(UpdateJobMainDTO dto) {
		Update update = new JobMainPO.Update();
		BeanUtils.copyProperties(dto, update);
		/**
		 * 被系统更新时不要更新updatedBy,updatedAt。可以用有没有身份来识别<br>
		 */
		if (SecurityUtils.getUsername() != null) {
			update.setUpdatedBy(SecurityUtils.getUsername());
			update.setUpdatedAt(SystemUtils.now());
		}

		return jobMainMapper.update(update) == 1;
	}

	/**
	 * 更新nextTrigAt超过给定的时间->状态未队列
	 * 
	 * @param nextTrigAtLt
	 * @return
	 */
	public int updateToNoQueuedByScan(LocalDateTime nextTrigAtLt) {
		return jobMainMapper.updateToNoQueuedByScan(nextTrigAtLt);
	}

	/**
	 * 更新所在实例实际已不存在的->状态未队列
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
		/**
		 * 被系统更新时不要更新updatedBy,updatedAt。可以用有没有身份来识别<br>
		 */
		if (SecurityUtils.getUsername() != null) {
			update.setUpdatedBy(SecurityUtils.getUsername());
			update.setUpdatedAt(SystemUtils.now());
		}

		return jobMainMapper.update(update) == 1;
	}

	public boolean updateEnQueue(UpdateJobMainEnQueueDTO dto) {
		dto.validate();

		Update update = new JobMainPO.Update();
		BeanUtils.copyProperties(dto, update);
		/**
		 * 被系统更新时不要更新updatedBy,updatedAt。可以用有没有身份来识别<br>
		 */
		if (SecurityUtils.getUsername() != null) {
			update.setUpdatedBy(SecurityUtils.getUsername());
			update.setUpdatedAt(SystemUtils.now());
		}

		/**
		 * 设为已队列
		 */
		update.setQueued(true);
		update.setQueuedAt(SystemUtils.now());

		return jobMainMapper.update(update) == 1;
	}

	private boolean updateRemoveQueue(Long jobId) {
		Update update = new JobMainPO.Update();
		/**
		 * 被系统更新时不要更新updatedBy,updatedAt。可以用有没有身份来识别<br>
		 */
		if (SecurityUtils.getUsername() != null) {
			update.setUpdatedBy(SecurityUtils.getUsername());
			update.setUpdatedAt(SystemUtils.now());
		}

		update.setQueued(false);
		update.setNextTrigAtNull(true);

		return jobMainMapper.update(update) == 1;
	}

	public boolean delete(Long id) {
		return jobMainMapper.delete(id) == 1;
	}

	/**
	 * EXPLAIN -- 总数 SELECT created_by, type, count( 0 ) AS count FROM job_main
	 * GROUP BY created_by, type
	 */
	@Deprecated
	public List<JobMainCountVO> countTotalGroupByTypeAndCreateBy() {
		JobMainCountQuery query = JobMainCountQuery.builder()
				.groupBy(JobMainCountQuery.GroupBy.builder().createdBy(true).type(true).build()).build();
		return (List) jobMainMapper.count(query);
	}

	public List<JobMainCountVO> countDayIncrGroupByTypeAndCreateBy() {
		LocalDateTime createdAtLt = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
		LocalDateTime createdAtGte = createdAtLt.minusDays(1);

		JobMainCountQuery query = JobMainCountQuery.builder().createdAtGte(createdAtGte).createdAtLt(createdAtLt)
				.groupBy(JobMainCountQuery.GroupBy.builder().createdBy(true).type(true).build()).build();
		return (List) jobMainMapper.count(query);
	}

	/**
	 * EXPLAIN -- 队列中 SELECT created_by, type, count( 0 ) AS count FROM job_main
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
	 * EXPLAIN -- 未队列未结束 SELECT created_by, type, count( 0 ) AS count FROM job_main
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
	 * EXPLAIN -- 已结束已成功 SELECT created_by, type, count( 0 ) AS count FROM job_main
	 * where is_end = 1 and is_last_execute_success = 1 GROUP BY created_by, type
	 * 
	 * @return
	 */
	@Deprecated
	public List<JobMainCountVO> countEndLastExecuteSuccessGroupByTypeAndCreateBy() {
		JobMainCountQuery query = JobMainCountQuery.builder().end(true).lastExecuteSuccess(true)
				.groupBy(JobMainCountQuery.GroupBy.builder().createdBy(true).type(true).build()).build();
		return (List) jobMainMapper.count(query);
	}

	public List<JobMainCountVO> countDayIncrEndLastExecuteSuccessGroupByTypeAndCreateBy() {
		LocalDateTime createdAtLt = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
		LocalDateTime createdAtGte = createdAtLt.minusDays(1);

		JobMainCountQuery query = JobMainCountQuery.builder().end(true).lastExecuteSuccess(true)
				.createdAtGte(createdAtGte).createdAtLt(createdAtLt)
				.groupBy(JobMainCountQuery.GroupBy.builder().createdBy(true).type(true).build()).build();
		return (List) jobMainMapper.count(query);
	}

	/**
	 * EXPLAIN -- 已结束未成功 SELECT created_by, type, count( 0 ) AS count FROM job_main
	 * WHERE is_end = 1 AND is_last_execute_success = 1 GROUP BY created_by, type
	 * 
	 * @return
	 */
	@Deprecated
	public List<JobMainCountVO> countEndLastExecuteFailedGroupByTypeAndCreateBy() {
		JobMainCountQuery query = JobMainCountQuery.builder().end(true).lastExecuteSuccess(false)
				.groupBy(JobMainCountQuery.GroupBy.builder().createdBy(true).type(true).build()).build();
		return (List) jobMainMapper.count(query);
	}

	public List<JobMainCountVO> countDayIncrEndLastExecuteFailedGroupByTypeAndCreateBy() {
		LocalDateTime createdAtLt = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
		LocalDateTime createdAtGte = createdAtLt.minusDays(1);

		JobMainCountQuery query = JobMainCountQuery.builder().end(true).lastExecuteSuccess(false)
				.createdAtGte(createdAtGte).createdAtLt(createdAtLt)
				.groupBy(JobMainCountQuery.GroupBy.builder().createdBy(true).type(true).build()).build();
		return (List) jobMainMapper.count(query);
	}
}

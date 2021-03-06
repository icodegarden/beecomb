package io.github.icodegarden.beecomb.master.controller.ruoyi;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.pojo.query.DelayJobQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobDetailQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.query.ScheduleJobQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO.Delay;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO.Schedule;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.CreateJobApiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.UpdateJobApiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.UpdateJobOpenapiDTO;
import io.github.icodegarden.beecomb.master.pojo.view.openapi.CreateJobOpenapiVO;
import io.github.icodegarden.beecomb.master.pojo.view.openapi.UpdateJobOpenapiVO;
import io.github.icodegarden.beecomb.master.ruoyi.AjaxResult;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;
import io.github.icodegarden.beecomb.master.service.JobReceiver;
import io.github.icodegarden.beecomb.master.service.JobFacadeManager;
import io.github.icodegarden.beecomb.master.service.JobLocalService;
import io.github.icodegarden.commons.lang.result.Result2;
import io.github.icodegarden.commons.lang.spec.response.ClientBizErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Controller
public class JobControllerRy extends BaseControllerRy {

	@Autowired
	private JobMainManager jobMainManager;
	@Autowired
	private JobFacadeManager jobFacadeManager;
	@Autowired
	private JobReceiver jobReceiver;
	@Autowired
	private JobLocalService jobLocalService;

	@GetMapping("view/job/list")
	public String jobList() {
		return "job/all/list";
	}

	@PostMapping(value = { "api/job/list" })
	public ResponseEntity<TableDataInfo> pageJobs(@RequestParam(required = false) Long id,
			@RequestParam(required = false) String uuid, @RequestParam(required = false) String nameLike,
			@RequestParam(required = false) JobType type, @RequestParam(required = false) Boolean parallel,
			@RequestParam(required = false) Boolean lastExecuteSuccess,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAtGte,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAtLte,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastTrigAtGte,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastTrigAtLte,
			@RequestParam(required = false) Boolean queued, @RequestParam(required = false) Boolean end,
			@RequestParam(defaultValue = "0") @Max(WebUtils.MAX_TOTAL_PAGES) int pageNum,
			@RequestParam(defaultValue = "10") @Max(WebUtils.MAX_PAGE_SIZE) int pageSize, /**
																							 * with
																							 */
			@RequestParam(required = false) boolean withQueuedAt,
			@RequestParam(required = false) boolean withQueuedAtInstance,
			@RequestParam(required = false) boolean withLastTrigResult,
			@RequestParam(required = false) boolean withLastExecuteExecutor,
			@RequestParam(required = false) boolean withLastExecuteReturns,
			@RequestParam(required = false) boolean withCreatedBy,
			@RequestParam(required = false) boolean withCreatedAt, @RequestParam(required = false) boolean withParams,
			@RequestParam(required = false) boolean withDesc, @RequestParam(required = false) boolean withDelay,
			@RequestParam(required = false) boolean withSchedule

	) {
		String username = SecurityUtils.getUsername();

		/**
		 * ????????????????????????
		 */
		JobMainQuery.With with = JobMainQuery.With.builder().createdAt(withCreatedAt).createdBy(withCreatedBy)
				.lastExecuteExecutor(withLastExecuteExecutor).queuedAt(withQueuedAt)
				.queuedAtInstance(withQueuedAtInstance)
				.jobDetail(JobDetailQuery.With.builder().desc(withDesc).params(withParams)
						.lastExecuteReturns(withLastExecuteReturns).lastTrigResult(withLastTrigResult).build())
				.delayJob(withDelay ? DelayJobQuery.With.builder().build() : null)
				.scheduleJob(withSchedule ? ScheduleJobQuery.With.builder().build() : null).build();

		JobMainQuery query = JobMainQuery.builder().id(id).uuid(uuid).nameLike(nameLike).type(type).parallel(parallel)
				.lastExecuteSuccess(lastExecuteSuccess).createdAtGte(createdAtGte).createdAtLte(createdAtLte)
				.lastTrigAtGte(lastTrigAtGte).lastTrigAtLte(lastTrigAtLte).queued(queued).end(end).createdBy(username)
				.page(pageNum).size(pageSize).sort("order by a.id desc").with(with).build();

		Page<JobMainVO> p = jobMainManager.page(query);

		return ResponseEntity.ok(getDataTable(p));
	}

	@GetMapping("view/job/{id}/detail")
	public String jobDetail(HttpServletRequest request, ModelMap mmap, @PathVariable Long id) {
		JobMainVO vo = jobMainManager.findOne(id, JobMainQuery.With.WITH_MOST);
		mmap.addAttribute("job", vo);
		return "job/all/detail";
	}

	@GetMapping("view/job/{id}/update")
	public String jobUpdate(HttpServletRequest request, ModelMap mmap, @PathVariable Long id) {
		JobMainVO vo = jobMainManager.findOne(id, JobMainQuery.With.WITH_MOST);
		mmap.addAttribute("job", vo);
		return "job/all/update";
	}

	@PostMapping(value = "api/job/update")
	public ResponseEntity<AjaxResult> updateJob(@Validated UpdateJobApiDTO dto) {
		try {
			ExecutableJobBO one = jobFacadeManager.findOneExecutableJob(dto.getId());

			UpdateJobOpenapiDTO updateJobOpenapiDTO = new UpdateJobOpenapiDTO();
			BeanUtils.copyProperties(dto, updateJobOpenapiDTO);

			UpdateJobOpenapiDTO.Delay delay = new UpdateJobOpenapiDTO.Delay();
			BeanUtils.copyProperties(dto, delay);
			updateJobOpenapiDTO.setDelay(delay);

			UpdateJobOpenapiDTO.Schedule schedule = new UpdateJobOpenapiDTO.Schedule();
			BeanUtils.copyProperties(dto, schedule);
			updateJobOpenapiDTO.setSchedule(schedule);

			boolean success = jobLocalService.update(updateJobOpenapiDTO, one);
			return ResponseEntity.ok(success ? success() : error());
		} catch (IllegalArgumentException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(400).body(e.getMessage());
		} catch (ErrorCodeException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
		}
	}

	@GetMapping("view/job/create")
	public String userCreate(HttpServletRequest request, ModelMap mmap) {
		return "job/all/create";
	}

	@PostMapping(value = "api/job/create")
	public ResponseEntity<AjaxResult> createJob(@Validated CreateJobApiDTO createJobApiDTO) {
		createJobApiDTO.validate();

		CreateJobDTO dto = new CreateJobDTO();
		BeanUtils.copyProperties(createJobApiDTO, dto);
		if (createJobApiDTO.getType() == JobType.Delay) {
			Delay delay = new CreateJobDTO.Delay();
			BeanUtils.copyProperties(createJobApiDTO, delay);
			dto.setDelay(delay);
		} else if (createJobApiDTO.getType() == JobType.Schedule) {
			Schedule schedule = new CreateJobDTO.Schedule();
			BeanUtils.copyProperties(createJobApiDTO, schedule);
			dto.setSchedule(schedule);
		} else {
			throw new RuntimeException("NOT IMPL type:" + createJobApiDTO.getType());
		}

		Result2<ExecutableJobBO, ErrorCodeException> result2 = jobReceiver.receive(dto);

		if (result2.isSuccess()) {
			ExecutableJobBO bo = result2.getT1();
			ErrorCodeException errorCodeException = result2.getT2();

			CreateJobOpenapiVO vo = CreateJobOpenapiVO.builder().job(CreateJobOpenapiVO.Job.of(bo))
					.dispatchException(errorCodeException != null ? errorCodeException.getMessage() : null).build();

			return ResponseEntity.ok(success());
		} else {
			ErrorCodeException errorCodeException = result2.getT2();
			log.error("ex on createjob by web", errorCodeException);
			return (ResponseEntity) ResponseEntity.status(errorCodeException.httpStatus())
					.body(errorCodeException.getMessage());
		}
	}

	@PostMapping("api/job/{id}/delete")
	public ResponseEntity<AjaxResult> deleteJob(@PathVariable Long id) {
		ExecutableJobBO one = jobFacadeManager.findOneExecutableJob(id);

		if (one == null) {
			return (ResponseEntity) ResponseEntity.status(404).body("Not Found");
		}
		/**
		 * ???????????????
		 */
		if (!SecurityUtils.getUsername().equals(one.getCreatedBy())) {
			return (ResponseEntity) ResponseEntity.status(404).body("Not Found, Ownership");
		}

		boolean delete = jobLocalService.delete(one);
		if (!delete) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN, "FORBIDDEN");
		}

		return ResponseEntity.ok(success());
	}
}

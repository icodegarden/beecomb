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
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.UpdateJobOpenapiDTO;
import io.github.icodegarden.beecomb.master.ruoyi.AjaxResult;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;
import io.github.icodegarden.beecomb.master.service.JobService;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Controller
public class JobControllerRy extends BaseControllerRy {

	@Autowired
	private JobMainManager jobMainManager;
	@Autowired
	private JobService jobService;

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
		 * 只查询对应用户的
		 */
		JobMainQuery.With with = JobMainQuery.With.builder().createdAt(withCreatedAt).createdBy(withCreatedBy)
				.lastExecuteExecutor(withLastExecuteExecutor).lastExecuteReturns(withLastExecuteReturns)
				.lastTrigResult(withLastTrigResult).queuedAt(withQueuedAt).queuedAtInstance(withQueuedAtInstance)
				.jobDetail(JobDetailQuery.With.builder().desc(withDesc).params(withParams).build())
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
	public ResponseEntity<AjaxResult> updateJob(@Validated UpdateJobDTO dto) {
		try {
			dto.validate();

			UpdateJobOpenapiDTO updateJobOpenapiDTO = new UpdateJobOpenapiDTO();
			BeanUtils.copyProperties(dto, updateJobOpenapiDTO);

			jobService.update(updateJobOpenapiDTO);
			return ResponseEntity.ok(success());
		} catch (IllegalArgumentException e) {
			/**
			 * 参数错误（包括唯一约束）400等
			 */
			return (ResponseEntity) ResponseEntity.status(400).body(e.getMessage());
		} catch (ErrorCodeException e) {
			/**
			 * 参数错误（包括唯一约束）400等
			 */
			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
		}
	}
}

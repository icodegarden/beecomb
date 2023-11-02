package io.github.icodegarden.beecomb.master.controller.ruoyi;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ConcurrentModel;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;

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
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.CreateJobApiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.UpdateJobApiDTO;
import io.github.icodegarden.beecomb.master.pojo.view.openapi.CreateJobOpenapiVO;
import io.github.icodegarden.beecomb.master.ruoyi.AjaxResult;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;
import io.github.icodegarden.beecomb.master.service.JobLocalService;
import io.github.icodegarden.beecomb.master.service.JobReceiver;
import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import io.github.icodegarden.nutrient.lang.result.Result2;
import io.github.icodegarden.nutrient.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;
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
	private JobReceiver jobReceiver;
	@Autowired
	private JobLocalService jobLocalService;

	@GetMapping("view/job/list")
	public String jobList(ServerWebExchange exchange) {
		return "job/all/list";
	}

	@PostMapping(value = { "api/job/list" })
	public ResponseEntity<TableDataInfo> pageJobs(//
			ServerWebExchange exchange
//			@RequestParam(required = false) Long id, @RequestParam(required = false) String uuid, //
//			@RequestParam(required = false) String nameLike, @RequestParam(required = false) String labelLike, //
//			@RequestParam(required = false) JobType type, @RequestParam(required = false) Boolean parallel, //
//			@RequestParam(required = false) Boolean lastExecuteSuccess, //
//			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAtGte, //
//			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAtLte, //
//			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastTrigAtGte, //
//			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastTrigAtLte, //
//			@RequestParam(required = false) Boolean queued, @RequestParam(required = false) Boolean end, //
//			@RequestParam(defaultValue = "0") @Max(BaseQuery.MAX_TOTAL_PAGES) int pageNum, //
//			@RequestParam(defaultValue = "10") @Max(BaseQuery.MAX_PAGE_SIZE) int pageSize, //
//			@RequestParam(required = false) boolean withQueuedAt, //
//			@RequestParam(required = false) boolean withQueuedAtInstance, //
//			@RequestParam(required = false) boolean withLastTrigResult, //
//			@RequestParam(required = false) boolean withLastExecuteExecutor, //
//			@RequestParam(required = false) boolean withLastExecuteReturns, //
//			@RequestParam(required = false) boolean withCreatedBy, //
//			@RequestParam(required = false) boolean withCreatedAt, //
//			@RequestParam(required = false) boolean withParams, //
//			@RequestParam(required = false) boolean withDesc, //
//			@RequestParam(required = false) boolean withDelay, //
//			@RequestParam(required = false) boolean withSchedule
	) {
		MultiValueMap<String, String> multiValueMap = getFormData(exchange);

		Long id = StringUtils.hasText(multiValueMap.getFirst("id")) ? Long.valueOf(multiValueMap.getFirst("id")) : null;
		String uuid = StringUtils.hasText(multiValueMap.getFirst("uuid")) ? multiValueMap.getFirst("uuid") : null;
		String nameLike = StringUtils.hasText(multiValueMap.getFirst("nameLike")) ? multiValueMap.getFirst("nameLike")
				: null;
		String labelLike = StringUtils.hasText(multiValueMap.getFirst("labelLike"))
				? multiValueMap.getFirst("labelLike")
				: null;
		JobType type = StringUtils.hasText(multiValueMap.getFirst("type"))
				? JobType.valueOf(multiValueMap.getFirst("type"))
				: null;
		Boolean parallel = StringUtils.hasText(multiValueMap.getFirst("parallel"))
				? Boolean.valueOf(multiValueMap.getFirst("parallel"))
				: null;
		Boolean lastExecuteSuccess = StringUtils.hasText(multiValueMap.getFirst("lastExecuteSuccess"))
				? Boolean.valueOf(multiValueMap.getFirst("lastExecuteSuccess"))
				: null;
		LocalDateTime createdAtGte = StringUtils.hasText(multiValueMap.getFirst("createdAtGte"))
				? LocalDateTime.parse(multiValueMap.getFirst("createdAtGte"), SystemUtils.STANDARD_DATETIME_FORMATTER)
				: null;
		LocalDateTime createdAtLte = StringUtils.hasText(multiValueMap.getFirst("createdAtLte"))
				? LocalDateTime.parse(multiValueMap.getFirst("createdAtLte"), SystemUtils.STANDARD_DATETIME_FORMATTER)
				: null;
		LocalDateTime lastTrigAtGte = StringUtils.hasText(multiValueMap.getFirst("lastTrigAtGte"))
				? LocalDateTime.parse(multiValueMap.getFirst("lastTrigAtGte"), SystemUtils.STANDARD_DATETIME_FORMATTER)
				: null;
		LocalDateTime lastTrigAtLte = StringUtils.hasText(multiValueMap.getFirst("lastTrigAtLte"))
				? LocalDateTime.parse(multiValueMap.getFirst("lastTrigAtLte"), SystemUtils.STANDARD_DATETIME_FORMATTER)
				: null;
		Boolean queued = StringUtils.hasText(multiValueMap.getFirst("queued"))
				? Boolean.valueOf(multiValueMap.getFirst("queued"))
				: null;
		Boolean end = StringUtils.hasText(multiValueMap.getFirst("end"))
				? Boolean.valueOf(multiValueMap.getFirst("end"))
				: null;
		int pageNum = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageNum")).orElse("0"));
		int pageSize = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageSize")).orElse("10"));
		boolean withQueuedAt = StringUtils.hasText(multiValueMap.getFirst("withQueuedAt"))
				? Boolean.parseBoolean(multiValueMap.getFirst("withQueuedAt"))
				: false;
		boolean withQueuedAtInstance = StringUtils.hasText(multiValueMap.getFirst("withQueuedAtInstance"))
				? Boolean.parseBoolean(multiValueMap.getFirst("withQueuedAtInstance"))
				: false;
		boolean withLastTrigResult = StringUtils.hasText(multiValueMap.getFirst("withLastTrigResult"))
				? Boolean.parseBoolean(multiValueMap.getFirst("withLastTrigResult"))
				: false;
		boolean withLastExecuteExecutor = StringUtils.hasText(multiValueMap.getFirst("withLastExecuteExecutor"))
				? Boolean.parseBoolean(multiValueMap.getFirst("withLastExecuteExecutor"))
				: false;
		boolean withLastExecuteReturns = StringUtils.hasText(multiValueMap.getFirst("withLastExecuteReturns"))
				? Boolean.parseBoolean(multiValueMap.getFirst("withLastExecuteReturns"))
				: false;
		boolean withCreatedBy = StringUtils.hasText(multiValueMap.getFirst("withCreatedBy"))
				? Boolean.parseBoolean(multiValueMap.getFirst("withCreatedBy"))
				: false;
		boolean withCreatedAt = StringUtils.hasText(multiValueMap.getFirst("withCreatedAt"))
				? Boolean.parseBoolean(multiValueMap.getFirst("withCreatedAt"))
				: false;
		boolean withParams = StringUtils.hasText(multiValueMap.getFirst("withParams"))
				? Boolean.parseBoolean(multiValueMap.getFirst("withParams"))
				: false;
		boolean withDesc = StringUtils.hasText(multiValueMap.getFirst("withDesc"))
				? Boolean.parseBoolean(multiValueMap.getFirst("withDesc"))
				: false;
		boolean withDelay = StringUtils.hasText(multiValueMap.getFirst("withDelay"))
				? Boolean.parseBoolean(multiValueMap.getFirst("withDelay"))
				: false;
		boolean withSchedule = StringUtils.hasText(multiValueMap.getFirst("withSchedule"))
				? Boolean.parseBoolean(multiValueMap.getFirst("withSchedule"))
				: false;

		String username = SecurityUtils.getUsername();

		/**
		 * 只查询对应用户的
		 */
		JobMainQuery.With with = JobMainQuery.With.builder().createdAt(withCreatedAt).createdBy(withCreatedBy)
				.lastExecuteExecutor(withLastExecuteExecutor).queuedAt(withQueuedAt)
				.queuedAtInstance(withQueuedAtInstance)
				.jobDetail(JobDetailQuery.With.builder().desc(withDesc).params(withParams)
						.lastExecuteReturns(withLastExecuteReturns).lastTrigResult(withLastTrigResult).build())
				.delayJob(withDelay ? DelayJobQuery.With.builder().build() : null)
				.scheduleJob(withSchedule ? ScheduleJobQuery.With.builder().build() : null).build();

		JobMainQuery query = JobMainQuery.builder().id(id).uuid(uuid).nameLike(nameLike).labelLike(labelLike).type(type)
				.parallel(parallel).lastExecuteSuccess(lastExecuteSuccess).createdAtGte(createdAtGte)
				.createdAtLte(createdAtLte).lastTrigAtGte(lastTrigAtGte).lastTrigAtLte(lastTrigAtLte).queued(queued)
				.end(end).createdBy(username).page(pageNum).size(pageSize).orderBy("a.id desc").with(with).build();

		Page<JobMainVO> p = jobMainManager.page(query);

		return ResponseEntity.ok(getDataTable(p));
	}

	@GetMapping("view/job/{id}/detail")
	public String jobDetail(ServerWebExchange exchange, ConcurrentModel mmap, @PathVariable Long id) {
		JobMainVO vo = jobMainManager.findOne(id, JobMainQuery.With.WITH_MOST);
		mmap.put("job", vo);
		return "job/all/detail";
	}

	@GetMapping("view/job/{id}/update")
	public String updateJobView(ServerWebExchange exchange, ConcurrentModel mmap, @PathVariable Long id) {
		JobMainVO vo = jobMainManager.findOne(id, JobMainQuery.With.WITH_MOST);
		mmap.put("job", vo);
		return "job/all/update";
	}

	@PostMapping(value = "api/job/update")
	public ResponseEntity<AjaxResult> updateJob(ServerWebExchange exchange, @Validated UpdateJobApiDTO dto) {
		try {
			UpdateJobDTO updateJobDTO = new UpdateJobDTO();
			BeanUtils.copyProperties(dto, updateJobDTO);

			UpdateJobDTO.Delay delay = new UpdateJobDTO.Delay();
			BeanUtils.copyProperties(dto, delay);
			updateJobDTO.setDelay(delay);

			UpdateJobDTO.Schedule schedule = new UpdateJobDTO.Schedule();
			BeanUtils.copyProperties(dto, schedule);
			updateJobDTO.setSchedule(schedule);

			boolean success = jobLocalService.updateByApi(updateJobDTO);
			return ResponseEntity.ok(success ? success() : error());
		} catch (IllegalArgumentException e) {
			/**
			 * 参数错误（包括唯一约束）400等
			 */
			return ResponseEntity.ok(AjaxResult.error(e.getMessage()));
		} catch (ErrorCodeException e) {
			/**
			 * 参数错误（包括唯一约束）400等
			 */
			return ResponseEntity.ok(AjaxResult.error(e.getSub_msg()));
		}
	}

	@GetMapping("view/job/create")
	public String createJobView(ServerWebExchange exchange, ConcurrentModel mmap) {
		return "job/all/create";
	}

	@PostMapping(value = "api/job/create")
	public ResponseEntity<AjaxResult> createJob(ServerWebExchange exchange, @Validated CreateJobApiDTO createJobApiDTO) {
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
			return ResponseEntity.ok(AjaxResult.error(errorCodeException.getSub_msg()));
		}
	}

	@PostMapping("api/job/{id}/reEnQueue")
	public ResponseEntity<AjaxResult> reEnQueueJob(ServerWebExchange exchange, @PathVariable Long id) {
		try {
			jobLocalService.reEnQueue(id);
		} catch (ErrorCodeException e) {
			/**
			 * ruoyi只认message字段
			 */
			return ResponseEntity.ok(AjaxResult.error(e.getSub_msg()));
		}
		return ResponseEntity.ok(success());
	}

	@PostMapping("api/job/{id}/run")
	public ResponseEntity<AjaxResult> runJob(ServerWebExchange exchange, @PathVariable Long id) {
		try {
			jobLocalService.runJob(id);
		} catch (ErrorCodeException e) {
			/**
			 * ruoyi只认message字段
			 */
			return ResponseEntity.ok(AjaxResult.error(e.getSub_msg()));
		}
		return ResponseEntity.ok(success());
	}

	@PostMapping("api/job/{id}/delete")
	public ResponseEntity<AjaxResult> deleteJob(ServerWebExchange exchange, @PathVariable Long id) {
		try {
			boolean delete = jobLocalService.delete(id);
			if (!delete) {
				return ResponseEntity.ok(AjaxResult.error("FORBIDDEN"));
			}
		} catch (ErrorCodeException e) {
			/**
			 * ruoyi只认message字段
			 */
			return ResponseEntity.ok(AjaxResult.error(e.getSub_msg()));
		}

		return ResponseEntity.ok(success());
	}
}

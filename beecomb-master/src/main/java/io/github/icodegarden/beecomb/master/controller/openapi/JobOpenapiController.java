package io.github.icodegarden.beecomb.master.controller.openapi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.CreateJobOpenapiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.UpdateJobOpenapiDTO;
import io.github.icodegarden.beecomb.master.pojo.view.openapi.CreateJobOpenapiVO;
import io.github.icodegarden.beecomb.master.pojo.view.openapi.DeleteJobOpenapiVO;
import io.github.icodegarden.beecomb.master.pojo.view.openapi.GetJobOpenapiVO;
import io.github.icodegarden.beecomb.master.pojo.view.openapi.PageJobsOpenapiVO;
import io.github.icodegarden.beecomb.master.pojo.view.openapi.UpdateJobOpenapiVO;
import io.github.icodegarden.beecomb.master.service.JobFacadeManager;
import io.github.icodegarden.beecomb.master.service.JobLocalService;
import io.github.icodegarden.beecomb.master.service.JobReceiver;
import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import io.github.icodegarden.nursery.springboot.web.util.WebUtils;
import io.github.icodegarden.nutrient.lang.query.BaseQuery;
import io.github.icodegarden.nutrient.lang.result.Result2;
import io.github.icodegarden.nutrient.lang.spec.response.ErrorCodeException;
import jakarta.validation.constraints.Max;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Validated
@RestController
public class JobOpenapiController {

	@Autowired
	private JobReceiver jobReceiver;
	@Autowired
	private JobMainManager jobMainManager;
	@Autowired
	private JobFacadeManager jobFacadeManager;
	@Autowired
	private JobLocalService jobLocalService;

	@PostMapping(value = { "openapi/v1/jobs" })
	public ResponseEntity<CreateJobOpenapiVO> createJob(ServerWebExchange exchange,
			@RequestParam(defaultValue = "true") boolean async, @RequestBody @Validated CreateJobOpenapiDTO dto) {
		dto.validate();

		Result2<ExecutableJobBO, ErrorCodeException> result2;
		if (async) {
			result2 = jobReceiver.receiveAsync(dto);
		} else {
			result2 = jobReceiver.receive(dto);
		}

		if (result2.isSuccess()) {
			ExecutableJobBO bo = result2.getT1();
			ErrorCodeException errorCodeException = result2.getT2();

			CreateJobOpenapiVO vo = CreateJobOpenapiVO.builder().job(CreateJobOpenapiVO.Job.of(bo))
					.dispatchException(errorCodeException != null ? errorCodeException.getMessage() : null).build();

			return ResponseEntity.ok(vo);
		} else {
			ErrorCodeException errorCodeException = result2.getT2();
			log.error("ex on createjob", errorCodeException);
			return (ResponseEntity) ResponseEntity.status(errorCodeException.httpStatus())
					.body(errorCodeException.getMessage());
		}
	}

	@GetMapping(value = { "openapi/v1/jobs" })
	public ResponseEntity<List<PageJobsOpenapiVO>> pageJobs(ServerWebExchange exchange,
			@RequestParam(required = false) String uuid, @RequestParam(required = false) String nameLike,
			@RequestParam(required = false) JobType type, @RequestParam(required = false) Boolean parallel,
			@RequestParam(required = false) Boolean lastExecuteSuccess,
			@RequestParam(required = false) LocalDateTime createdAtGte,
			@RequestParam(required = false) LocalDateTime createdAtLte,
			@RequestParam(required = false) LocalDateTime lastTrigAtGte,
			@RequestParam(required = false) LocalDateTime lastTrigAtLte, @RequestParam(required = false) Boolean queued,
			@RequestParam(required = false) Boolean end,
			@RequestParam(defaultValue = "0") @Max(BaseQuery.MAX_TOTAL_PAGES) int page,
			@RequestParam(defaultValue = "10") @Max(BaseQuery.MAX_PAGE_SIZE) int size, /**
																						 * with
																						 */
			@RequestParam(required = false) boolean withQueuedAt,
			@RequestParam(required = false) boolean withQueuedAtInstance,
			@RequestParam(required = false) boolean withLastTrigResult,
			@RequestParam(required = false) boolean withLastExecuteExecutor,
			@RequestParam(required = false) boolean withLastExecuteReturns,
			@RequestParam(required = false) boolean withLastExecuteSuccess,
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
				.lastExecuteExecutor(withLastExecuteExecutor).queuedAt(withQueuedAt)
				.queuedAtInstance(withQueuedAtInstance)
				.jobDetail(JobDetailQuery.With.builder().desc(withDesc).params(withParams)
						.lastExecuteReturns(withLastExecuteReturns).lastTrigResult(withLastTrigResult).build())
				.delayJob(withDelay ? DelayJobQuery.With.builder().build() : null)
				.scheduleJob(withSchedule ? ScheduleJobQuery.With.builder().build() : null).build();

		JobMainQuery query = JobMainQuery.builder().uuid(uuid).nameLike(nameLike).type(type).parallel(parallel)
				.lastExecuteSuccess(lastExecuteSuccess).createdAtGte(createdAtGte).createdAtLte(createdAtLte)
				.lastTrigAtGte(lastTrigAtGte).lastTrigAtLte(lastTrigAtLte).queued(queued).end(end).createdBy(username)
				.page(page).size(size).orderBy("a.id desc").with(with).build();

		Page<JobMainVO> p = jobMainManager.page(query);

		List<PageJobsOpenapiVO> list = p.getResult().stream().map(one -> PageJobsOpenapiVO.of(one))
				.collect(Collectors.toList());

		return new ResponseEntity<List<PageJobsOpenapiVO>>(list, WebUtils.pageHeaders(p.getPages(), p.getTotal()),
				HttpStatus.OK);
	}

	@GetMapping(value = { "openapi/v1/jobs/{id}" })
	public ResponseEntity<GetJobOpenapiVO> getJob(ServerWebExchange exchange, @PathVariable Long id) {
		JobMainVO one = jobMainManager.findOne(id, JobMainQuery.With.WITH_MOST);

		if (one == null) {
			return (ResponseEntity) ResponseEntity.status(404).body("Not Found");
		}
		/**
		 * 校验归属权
		 */
		if (!SecurityUtils.getUsername().equals(one.getCreatedBy())) {
			return (ResponseEntity) ResponseEntity.status(404).body("Not Found, Ownership");
		}

		GetJobOpenapiVO vo = GetJobOpenapiVO.of(one);

		return ResponseEntity.ok(vo);
	}

	@GetMapping(value = { "openapi/v1/jobs/uuid/{uuid}" })
	public ResponseEntity<GetJobOpenapiVO> getJobByUUID(ServerWebExchange exchange, @PathVariable String uuid) {
		JobMainVO one = jobMainManager.findByUUID(uuid, JobMainQuery.With.WITH_MOST);

		if (one == null) {
			return (ResponseEntity) ResponseEntity.status(404).body("Not Found");
		}
		/**
		 * 校验归属权
		 */
		if (!SecurityUtils.getUsername().equals(one.getCreatedBy())) {
			return (ResponseEntity) ResponseEntity.status(404).body("Not Found, Ownership");
		}

		GetJobOpenapiVO vo = GetJobOpenapiVO.of(one);

		return ResponseEntity.ok(vo);
	}

	@PutMapping(value = { "openapi/v1/jobs" })
	public ResponseEntity<UpdateJobOpenapiVO> updateJob(ServerWebExchange exchange,
			@RequestBody @Validated UpdateJobOpenapiDTO dto) {
		dto.validate();

		boolean success = jobLocalService.updateByApi(dto);
		UpdateJobOpenapiVO vo = new UpdateJobOpenapiVO(dto.getId(), success);
		return ResponseEntity.ok(vo);
	}

	@DeleteMapping(value = { "openapi/v1/jobs/{id}" })
	public ResponseEntity<DeleteJobOpenapiVO> deleteJob(ServerWebExchange exchange, @PathVariable Long id) {
		boolean success = jobLocalService.delete(id);
		DeleteJobOpenapiVO vo = new DeleteJobOpenapiVO(id, success);
		return ResponseEntity.ok(vo);
	}
}

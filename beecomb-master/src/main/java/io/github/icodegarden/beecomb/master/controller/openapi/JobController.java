package io.github.icodegarden.beecomb.master.controller.openapi;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.Max;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.db.pojo.query.JobQuery;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobWith;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.core.JobReceiver;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.view.CreateJobVO;
import io.github.icodegarden.beecomb.master.pojo.view.JobVO;
import io.github.icodegarden.beecomb.master.service.JobService;
import io.github.icodegarden.commons.lang.result.Result2;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Validated
@RestController
public class JobController {

	@Autowired
	private JobReceiver jobReceiver;
	@Autowired
	private JobService jobService;

	@PostMapping(value = { "openapi/v1/jobs" })
	public ResponseEntity<CreateJobVO> createJob(@RequestParam(defaultValue = "true") boolean async,
			@RequestBody @Validated CreateJobDTO dto) {
		Result2<ExecutableJobBO, ErrorCodeException> result2;
		if (async) {
			result2 = jobReceiver.receiveAsync(dto);
		} else {
			result2 = jobReceiver.receive(dto);
		}

		if (result2.isSuccess()) {
			ExecutableJobBO jobVO = result2.getT1();
			ErrorCodeException errorCodeException = result2.getT2();

			CreateJobVO createJobVO = new CreateJobVO();
			createJobVO.setJob(new CreateJobVO.Job(jobVO));
			if (errorCodeException != null) {
				createJobVO.setDispatchException(errorCodeException.getMessage());
			}
			return ResponseEntity.ok(createJobVO);
		} else {
			ErrorCodeException errorCodeException = result2.getT2();
			CreateJobVO createJobVO = new CreateJobVO();
			createJobVO.setDispatchException(errorCodeException.getMessage());
			return ResponseEntity.status(errorCodeException.httpStatus()).body(createJobVO);
		}
	}

	@GetMapping(value = { "openapi/v1/jobs" })
	public ResponseEntity<List<JobVO>> pageJobs(@RequestParam(required = false) String uuid,
			@RequestParam(required = false) String nameLike, @RequestParam(required = false) JobType type,
			@RequestParam(required = false) Boolean parallel,
			@RequestParam(required = false) Boolean lastExecuteSuccess,
			@RequestParam(required = false) LocalDateTime createdAtGte,
			@RequestParam(required = false) LocalDateTime createdAtLte,
			@RequestParam(required = false) LocalDateTime lastTrigAtGte,
			@RequestParam(required = false) LocalDateTime lastTrigAtLte, @RequestParam(required = false) Boolean queued,
			@RequestParam(required = false) Boolean end,
			@RequestParam(defaultValue = "0") @Max(WebUtils.MAX_TOTAL_PAGES) int page,
			@RequestParam(defaultValue = "10") @Max(WebUtils.MAX_PAGE_SIZE) int size, /**
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
		JobWith with = JobWith.builder()
				.jobMain(JobWith.JobMain.builder().createdAt(withCreatedAt).createdBy(withCreatedBy)
						.lastExecuteExecutor(withLastExecuteExecutor).lastExecuteReturns(withLastExecuteReturns)
						.lastTrigResult(withLastTrigResult).queuedAt(withQueuedAt)
						.queuedAtInstance(withQueuedAtInstance).build())
				.jobDetail(JobWith.JobDetail.builder().desc(withDesc).params(withParams).build())
				.delayJob(withDelay ? JobWith.DelayJob.builder().build() : null)
				.scheduleJob(withSchedule ? JobWith.ScheduleJob.builder().build() : null).build();

		JobQuery query = JobQuery.builder().uuid(uuid).nameLike(nameLike).type(type).parallel(parallel)
				.lastExecuteSuccess(lastExecuteSuccess).createdAtGte(createdAtGte).createdAtLte(createdAtLte)
				.lastTrigAtGte(lastTrigAtGte).lastTrigAtLte(lastTrigAtLte).queued(queued).end(end).createdBy(username)
				.page(page).size(size).sort("order by a.id desc").with(with).build();

		Page<JobVO> p = jobService.page(query);

		return new ResponseEntity<List<JobVO>>(p.getResult(), WebUtils.pageHeaders(p.getPages(), p.getTotal()),
				HttpStatus.OK);
	}

	@GetMapping(value = { "openapi/v1/jobs/{id}" })
	public ResponseEntity<JobVO> findOne(@PathVariable Long id) {
		JobVO vo = jobService.findOne(id, JobWith.WITH_MOST);

		/**
		 * 校验归属权
		 */
		if (!SecurityUtils.getUsername().equals(vo.getCreatedBy())) {
			return (ResponseEntity) ResponseEntity.status(404).body("Not Found, Ownership");
		}

		return ResponseEntity.ok(vo);
	}

	@GetMapping(value = { "openapi/v1/jobs/uuid/{uuid}" })
	public ResponseEntity<JobVO> findByUUID(@PathVariable String uuid) {
		JobVO vo = jobService.findByUUID(uuid, JobWith.WITH_MOST);

		/**
		 * 校验归属权
		 */
		if (!SecurityUtils.getUsername().equals(vo.getCreatedBy())) {
			return (ResponseEntity) ResponseEntity.status(404).body("Not Found, Ownership");
		}

		return ResponseEntity.ok(vo);
	}

//	@PutMapping(value = { "openapi/v1/jobs"})
//	public ResponseEntity<Void> updateJob(@RequestBody @Validated UpdateJobDTO dto) {
//		
//	}
}

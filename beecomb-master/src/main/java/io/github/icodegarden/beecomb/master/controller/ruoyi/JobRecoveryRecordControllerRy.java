package io.github.icodegarden.beecomb.master.controller.ruoyi;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ConcurrentModel;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.master.manager.JobRecoveryRecordManager;
import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordQuery;
import io.github.icodegarden.beecomb.master.pojo.view.JobRecoveryRecordVO;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;
import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Controller
public class JobRecoveryRecordControllerRy extends BaseControllerRy {

	@Autowired
	private JobRecoveryRecordManager jobRecoveryRecordService;

	@GetMapping("view/jobRecoveryRecord/list")
	public String jobRecoveryRecordList(ServerWebExchange exchange, ConcurrentModel mmap,
			@RequestParam(required = false) Long jobId) {
		mmap.put("jobId", jobId);
		return "job/jobRecoveryRecord/list";
	}

	@PostMapping("api/jobRecoveryRecord/list")
	public Mono<ResponseEntity<TableDataInfo>> pageJobRecoveryRecords(ServerWebExchange exchange
//			@RequestParam(required = false) Long jobId,
//			@RequestParam(required = false) Boolean success,
//			@RequestParam(defaultValue = "0") @Max(BaseQuery.MAX_TOTAL_PAGES) int pageNum,
//			@RequestParam(defaultValue = "10") @Max(BaseQuery.MAX_PAGE_SIZE) int pageSize
	) {
//		MultiValueMap<String, String> multiValueMap = getFormData(exchange);

		return exchange.getFormData().map(multiValueMap -> {
			Long jobId = StringUtils.hasText(multiValueMap.getFirst("jobId"))
					? Long.valueOf(multiValueMap.getFirst("jobId"))
					: null;
			Boolean success = StringUtils.hasText(multiValueMap.getFirst("success"))
					? Boolean.valueOf(multiValueMap.getFirst("success"))
					: null;
			int pageNum = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageNum")).orElse("0"));
			int pageSize = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageSize")).orElse("10"));

			String username = SecurityUtils.getUsername();

			JobRecoveryRecordQuery query = JobRecoveryRecordQuery.builder().jobId(jobId).success(success)
					.jobCreatedBy(username)
					.with(JobRecoveryRecordQuery.With.builder()
							.jobMain(JobRecoveryRecordQuery.With.JobMain.builder().build()).build())
					.page(pageNum).size(pageSize).orderBy("a.recovery_at desc").build();

			Page<JobRecoveryRecordVO> p = jobRecoveryRecordService.page(query);
			return ResponseEntity.ok(getDataTable(p));
		});
	}

	@GetMapping("view/jobRecoveryRecord/jobs/{jobId}/detail")
	public String jobRecoveryRecordDetail(ServerWebExchange exchange, ConcurrentModel mmap, @PathVariable Long jobId) {
		JobRecoveryRecordVO vo = jobRecoveryRecordService.findOne(jobId, JobRecoveryRecordQuery.With.WITH_MOST);
		mmap.put("record", vo);
		return "job/jobRecoveryRecord/detail";
	}

}

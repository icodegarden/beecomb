package io.github.icodegarden.beecomb.master.controller.ruoyi;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.master.manager.JobRecoveryRecordManager;
import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordQuery;
import io.github.icodegarden.beecomb.master.pojo.view.JobRecoveryRecordVO;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;
import io.github.icodegarden.commons.lang.query.BaseQuery;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;

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
	public String jobRecoveryRecordList(HttpServletRequest request, ModelMap mmap, @RequestParam(required = false) Long jobId) {
		mmap.addAttribute("jobId", jobId);
		return "job/jobRecoveryRecord/list";
	}

	@PostMapping("api/jobRecoveryRecord/list")
	public ResponseEntity<TableDataInfo> pageJobRecoveryRecords(@RequestParam(required = false) Long jobId,
			@RequestParam(required = false) Boolean success,
			@RequestParam(defaultValue = "0") @Max(BaseQuery.MAX_TOTAL_PAGES) int pageNum,
			@RequestParam(defaultValue = "10") @Max(BaseQuery.MAX_PAGE_SIZE) int pageSize) {
		String username = SecurityUtils.getUsername();

		JobRecoveryRecordQuery query = JobRecoveryRecordQuery.builder().jobId(jobId).success(success)
				.jobCreatedBy(username)
				.with(JobRecoveryRecordQuery.With.builder()
						.jobMain(JobRecoveryRecordQuery.With.JobMain.builder().build()).build())
				.page(pageNum).size(pageSize).sort("order by a.recovery_at desc").build();

		Page<JobRecoveryRecordVO> p = jobRecoveryRecordService.page(query);
		return ResponseEntity.ok(getDataTable(p));
	}

	@GetMapping("view/jobRecoveryRecord/jobs/{jobId}/detail")
	public String jobRecoveryRecordDetail(HttpServletRequest request, ModelMap mmap, @PathVariable Long jobId) {
		JobRecoveryRecordVO vo = jobRecoveryRecordService.findOne(jobId, JobRecoveryRecordQuery.With.WITH_MOST);
		mmap.addAttribute("record", vo);
		return "job/jobRecoveryRecord/detail";
	}

}

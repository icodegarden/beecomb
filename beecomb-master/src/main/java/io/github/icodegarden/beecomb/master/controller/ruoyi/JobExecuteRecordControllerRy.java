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

import io.github.icodegarden.beecomb.common.backend.manager.JobExecuteRecordManager;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobExecuteRecordQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobExecuteRecordVO;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Controller
public class JobExecuteRecordControllerRy extends BaseControllerRy {

	@Autowired
	private JobExecuteRecordManager jobExecuteRecordManager;

	@GetMapping("view/jobExecuteRecord/list")
	public String jobExecuteRecordList(HttpServletRequest request, ModelMap mmap,
			@RequestParam(required = false) Long jobId) {
		mmap.addAttribute("jobId", jobId);
		return "/job/jobExecuteRecord/list";
	}

	@PostMapping("api/jobExecuteRecord/list")
	public ResponseEntity<TableDataInfo> pageJobExecuteRecords(@RequestParam(required = false) Long jobId,
			@RequestParam(required = false) Boolean success,
			@RequestParam(defaultValue = "0") @Max(WebUtils.MAX_TOTAL_PAGES) int pageNum,
			@RequestParam(defaultValue = "10") @Max(WebUtils.MAX_PAGE_SIZE) int pageSize) {
		JobExecuteRecordQuery query = JobExecuteRecordQuery.builder().jobId(jobId).success(success).page(pageNum)
				.size(pageSize).sort("order by a.id desc").build();

		Page<JobExecuteRecordVO> p = jobExecuteRecordManager.page(query);
		return ResponseEntity.ok(getDataTable(p));
	}

	@GetMapping("view/jobExecuteRecord/{id}/detail")
	public String jobExecuteRecordDetail(HttpServletRequest request, ModelMap mmap, @PathVariable Long id) {
		JobExecuteRecordVO vo = jobExecuteRecordManager.findOne(id,
				JobExecuteRecordQuery.With.builder().executeExecutor(true).executeReturns(true).build());

		mmap.addAttribute("record", vo);
		return "/job/jobExecuteRecord/detail";
	}

}

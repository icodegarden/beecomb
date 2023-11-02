package io.github.icodegarden.beecomb.master.controller.ruoyi;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ConcurrentModel;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.backend.manager.JobExecuteRecordManager;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobExecuteRecordQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobExecuteRecordVO;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;

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
	public String jobExecuteRecordList(ServerWebExchange exchange, ConcurrentModel mmap, @RequestParam(required = false) Long jobId) {
		mmap.put("jobId", jobId);
		return "job/jobExecuteRecord/list";
	}

	@PostMapping("api/jobExecuteRecord/list")
	public ResponseEntity<TableDataInfo> pageJobExecuteRecords(ServerWebExchange exchange
//			@RequestParam(required = false) Long jobId,
//			@RequestParam(required = false) Boolean success,
//			@RequestParam(defaultValue = "0") @Max(BaseQuery.MAX_TOTAL_PAGES) int pageNum,
//			@RequestParam(defaultValue = "10") @Max(BaseQuery.MAX_PAGE_SIZE) int pageSize
	) {
		MultiValueMap<String, String> multiValueMap = getFormData(exchange);

		Long jobId = StringUtils.hasText(multiValueMap.getFirst("jobId"))
				? Long.valueOf(multiValueMap.getFirst("jobId"))
				: null;
		Boolean success = StringUtils.hasText(multiValueMap.getFirst("success"))
				? Boolean.valueOf(multiValueMap.getFirst("success"))
				: null;
		int pageNum = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageNum")).orElse("0"));
		int pageSize = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageSize")).orElse("10"));

		JobExecuteRecordQuery query = JobExecuteRecordQuery.builder().jobId(jobId).success(success).page(pageNum)
				.size(pageSize).orderBy("a.id desc").build();

		Page<JobExecuteRecordVO> p = jobExecuteRecordManager.page(query);
		return ResponseEntity.ok(getDataTable(p));
	}

	@GetMapping("view/jobExecuteRecord/{id}/detail")
	public String jobExecuteRecordDetail(ServerWebExchange exchange, ConcurrentModel mmap, @PathVariable Long id) {
		JobExecuteRecordVO vo = jobExecuteRecordManager.findOne(id,
				JobExecuteRecordQuery.With.builder().trigResult(true).executeReturns(true).build());

		mmap.put("record", vo);
		return "job/jobExecuteRecord/detail";
	}

}

//package io.github.icodegarden.beecomb.master.controller;
//
//import java.util.List;
//
//import javax.validation.constraints.Max;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.github.pagehelper.Page;
//
//import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordQuery;
//import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordWith;
//import io.github.icodegarden.beecomb.master.pojo.query.JobRecoveryRecordWith.JobMain;
//import io.github.icodegarden.beecomb.master.pojo.view.JobRecoveryRecordVO;
//import io.github.icodegarden.beecomb.master.security.SecurityUtils;
//import io.github.icodegarden.beecomb.master.service.JobRecoveryRecordService;
//import io.github.icodegarden.commons.springboot.web.util.WebUtils;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//@Validated
//@RestController
//public class JobRecoveryRecordController {
//
//	@Autowired
//	private JobRecoveryRecordService jobRecoveryRecordService;
//
//	@GetMapping("api/v1/jobRecoveryRecords")
//	public ResponseEntity<List<JobRecoveryRecordVO>> pageJobRecoveryRecords(
//			@RequestParam(required = false) Boolean success,
//			@RequestParam(defaultValue = "0") @Max(WebUtils.MAX_TOTAL_PAGES) int page,
//			@RequestParam(defaultValue = "10") @Max(WebUtils.MAX_PAGE_SIZE) int size) {
//		String username = SecurityUtils.getUsername();
//
//		JobRecoveryRecordQuery query = JobRecoveryRecordQuery.builder().success(success).jobCreatedBy(username)
//				.with(JobRecoveryRecordWith.builder().jobMain(JobMain.builder().build()).build()).page(page).size(size)
//				.sort("order by a.created_at desc").build();
//
//		Page<JobRecoveryRecordVO> p = jobRecoveryRecordService.page(query);
//		return new ResponseEntity<List<JobRecoveryRecordVO>>(p.getResult(),
//				WebUtils.pageHeaders(p.getPages(), p.getTotal()), HttpStatus.OK);
//	}
//
//	@GetMapping("api/v1/jobRecoveryRecords/jobs/{jobId}")
//	public ResponseEntity<JobRecoveryRecordVO> findJobRecoveryRecord(@PathVariable Long jobId) {
//		JobRecoveryRecordVO vo = jobRecoveryRecordService.findOne(jobId, JobRecoveryRecordWith.WITH_MOST);
//
//		if (vo.getJob() != null) {
//			/**
//			 * 校验归属权
//			 */
//			if (!SecurityUtils.getUsername().equals(vo.getJob().getCreatedBy())) {
//				return (ResponseEntity) ResponseEntity.status(404).body("Not Found, Ownership");
//			}
//		}
//
//		return ResponseEntity.ok(vo);
//	}
//}

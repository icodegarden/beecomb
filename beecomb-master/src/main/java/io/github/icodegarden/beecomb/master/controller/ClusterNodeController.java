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
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.github.pagehelper.Page;
//
//import io.github.icodegarden.beecomb.master.pojo.query.ClusterNodeQuery;
//import io.github.icodegarden.beecomb.master.pojo.view.ClusterNodeVO;
//import io.github.icodegarden.beecomb.master.service.ClusterNodeService;
//import io.github.icodegarden.commons.springboot.web.util.WebUtils;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//@Validated
//@RestController
//public class ClusterNodeController {
//
//	@Autowired
//	private ClusterNodeService clusterNodeService;
//
//	@GetMapping("api/v1/clusterNodes")
//	public ResponseEntity<List<ClusterNodeVO>> pageUsers(@RequestParam String serviceName,
//			@RequestParam(defaultValue = "0") @Max(WebUtils.MAX_TOTAL_PAGES) int page,
//			@RequestParam(defaultValue = "10") @Max(WebUtils.MAX_PAGE_SIZE) int size) {
//		ClusterNodeQuery query = ClusterNodeQuery.builder().page(page).size(size).serviceName(serviceName).build();
//		Page<ClusterNodeVO> p = clusterNodeService.pageNodes(query);
//
//		return new ResponseEntity<List<ClusterNodeVO>>(p.getResult(), WebUtils.pageHeaders(p.getPages(), p.getTotal()),
//				HttpStatus.OK);
//	}
//
//}

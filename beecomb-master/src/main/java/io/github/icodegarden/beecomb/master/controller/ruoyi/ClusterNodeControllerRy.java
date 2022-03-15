package io.github.icodegarden.beecomb.master.controller.ruoyi;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.master.pojo.query.ClusterNodeQuery;
import io.github.icodegarden.beecomb.master.pojo.view.ClusterNodeVO;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;
import io.github.icodegarden.beecomb.master.service.ClusterNodeService;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Controller
public class ClusterNodeControllerRy extends BaseControllerRy {

	@Autowired
	private ClusterNodeService clusterNodeService;

	@GetMapping("view/node/list")
	public String nodeList(HttpServletRequest request, ModelMap mmap) {
		return "/cluster/node/list";
	}

	@PostMapping("api/node/list")
	public ResponseEntity<TableDataInfo> pageNodes(@RequestParam String serviceName,
			@RequestParam(required = false) String ip,
			@RequestParam(defaultValue = "0") @Max(WebUtils.MAX_TOTAL_PAGES) int pageNum,
			@RequestParam(defaultValue = "10") @Max(WebUtils.MAX_PAGE_SIZE) int pageSize) {
		ClusterNodeQuery query = ClusterNodeQuery.builder().page(pageNum).size(pageSize).serviceName(serviceName).ip(ip)
				.build();
		Page<ClusterNodeVO> p = clusterNodeService.pageNodes(query);

		return ResponseEntity.ok(getDataTable(p));
	}
}

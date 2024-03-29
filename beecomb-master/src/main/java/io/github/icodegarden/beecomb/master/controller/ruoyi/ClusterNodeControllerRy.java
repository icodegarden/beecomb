package io.github.icodegarden.beecomb.master.controller.ruoyi;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ConcurrentModel;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.master.manager.ClusterNodeManager;
import io.github.icodegarden.beecomb.master.pojo.query.ClusterNodeQuery;
import io.github.icodegarden.beecomb.master.pojo.view.ClusterNodeVO;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Controller
public class ClusterNodeControllerRy extends BaseControllerRy {

	@Autowired
	private ClusterNodeManager clusterNodeService;

	@GetMapping("view/node/list")
	public String nodeList(ServerWebExchange exchange, ConcurrentModel mmap) {
		return "cluster/node/list";
	}

	@PostMapping("api/node/list")
	public Mono<ResponseEntity<TableDataInfo>> pageNodes(ServerWebExchange exchange
//			@RequestParam String serviceName,
//			@RequestParam(required = false) String ip,
//			@RequestParam(defaultValue = "0") @Max(BaseQuery.MAX_TOTAL_PAGES) int pageNum,
//			@RequestParam(defaultValue = "10") @Max(BaseQuery.MAX_PAGE_SIZE) int pageSize
	) {
//		MultiValueMap<String, String> multiValueMap = getFormData(exchange);

		return exchange.getFormData().map(multiValueMap -> {
			String serviceName = StringUtils.hasText(multiValueMap.getFirst("serviceName"))
					? multiValueMap.getFirst("serviceName")
					: null;
			String executorName = StringUtils.hasText(multiValueMap.getFirst("executorName"))
					? multiValueMap.getFirst("executorName")
					: null;
			String ip = StringUtils.hasText(multiValueMap.getFirst("ip")) ? multiValueMap.getFirst("ip") : null;
			int pageNum = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageNum")).orElse("0"));
			int pageSize = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageSize")).orElse("10"));

			ClusterNodeQuery query = ClusterNodeQuery.builder().page(pageNum).size(pageSize).serviceName(serviceName)
					.executorName(executorName).ip(ip).build();
			Page<ClusterNodeVO> p = clusterNodeService.pageNodes(query);

			return ResponseEntity.ok(getDataTable(p));
		});
	}

}

package io.github.icodegarden.beecomb.master.controller.ruoyi;

import java.util.List;
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

import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.master.manager.ClusterNodeManager;
import io.github.icodegarden.beecomb.master.pojo.query.ClusterNodeQuery;
import io.github.icodegarden.beecomb.master.pojo.view.ClusterNodeVO;
import io.github.icodegarden.beecomb.master.ruoyi.AjaxResult;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;
import io.github.icodegarden.beecomb.master.service.ShardingService;
import io.github.icodegarden.nutrient.lang.spec.response.ClientBizErrorCodeException;
import io.github.icodegarden.nutrient.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.nutrient.shardingsphere.builder.RangeModShardingAlgorithmConfig.Group;
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
	@Autowired
	private ShardingService shardingService;

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

	@GetMapping("view/sharding/list")
	public String shardingList(ServerWebExchange exchange, ConcurrentModel mmap) {
		return "cluster/sharding/list";
	}

	@PostMapping("api/sharding/list")
	public Mono<ResponseEntity<TableDataInfo>> pageShards(ServerWebExchange exchange
//			@RequestParam String serviceName,
//			@RequestParam(required = false) String ip,
//			@RequestParam(defaultValue = "0") @Max(BaseQuery.MAX_TOTAL_PAGES) int pageNum,
//			@RequestParam(defaultValue = "10") @Max(BaseQuery.MAX_PAGE_SIZE) int pageSize
	) {
//		MultiValueMap<String, String> multiValueMap = getFormData(exchange);

		return exchange.getFormData().map(multiValueMap -> {
			String groupLike = StringUtils.hasText(multiValueMap.getFirst("groupLike"))
					? multiValueMap.getFirst("groupLike")
					: null;
//			String executorName = StringUtils.hasText(multiValueMap.getFirst("executorName"))
//					? multiValueMap.getFirst("executorName")
//					: null;
//			String ip = StringUtils.hasText(multiValueMap.getFirst("ip")) ? multiValueMap.getFirst("ip") : null;
//			int pageNum = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageNum")).orElse("0"));
//			int pageSize = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageSize")).orElse("10"));

			List<Group> groups = shardingService.listGroups(groupLike);
			return ResponseEntity.ok(getDataTable(groups));
		});
	}

	@GetMapping("view/sharding/{name}/lastJob")
	public String getLastJob(ServerWebExchange exchange, ConcurrentModel mmap, @PathVariable String name) {
		JobMainVO vo = shardingService.getLastJob(name);
		if (vo == null) {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.NOT_FOUND, "Job Not Exist.");
		}
		mmap.put("job", vo);
		return "cluster/sharding/lastJob";
	}

	@GetMapping("view/sharding/notEndJob/list")
	public String jobList(ServerWebExchange exchange, ConcurrentModel mmap,
			@RequestParam(required = true) String group) {
		mmap.put("group", group);
		return "cluster/sharding/notEndJobList";
	}

	@PostMapping(value = { "api/sharding/notEndJob/list" })
	public Mono<ResponseEntity<TableDataInfo>> pageNotEndJobs(//
			ServerWebExchange exchange) {

		return exchange.getFormData().map(multiValueMap -> {
			String group = StringUtils.hasText(multiValueMap.getFirst("group")) ? multiValueMap.getFirst("group")
					: null;

			Page<JobMainVO> p = shardingService.pageNotEndJobs(group);

			return ResponseEntity.ok(getDataTable(p));
		});
	}
	
	@PostMapping(value = { "api/sharding/{group}/deleteEndJobs" })
	public ResponseEntity<AjaxResult> deleteEndJobs(ServerWebExchange exchange, @PathVariable String group) {
		try {
			shardingService.deleteEndJobs(group);
		} catch (ErrorCodeException e) {
			/**
			 * ruoyi只认message字段
			 */
			return ResponseEntity.ok(AjaxResult.error(e.getSub_msg()));
		}

		return ResponseEntity.ok(success());
	}
}

package io.github.icodegarden.beecomb.master.controller.ruoyi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import io.github.icodegarden.beecomb.master.manager.ReportLineManager;
import io.github.icodegarden.beecomb.master.pojo.persistence.ReportLinePO;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.PlatformRole;
import io.github.icodegarden.beecomb.master.ruoyi.AjaxResult;
import io.github.icodegarden.beecomb.master.security.UserUtils;
import io.github.icodegarden.beecomb.master.service.ReportService;
import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import io.github.icodegarden.nutrient.lang.util.JsonUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Controller
public class ReportControllerRy extends BaseControllerRy {

	@Autowired
	private ReportService reportService;
	@Autowired
	private ReportLineManager reportLineManager;

	@PostMapping(value = "api/report/type/{type}/detail")
	public ResponseEntity<ReportLinePO> getReportByType(@PathVariable ReportLinePO.Type type) {
		ReportLinePO line = reportLineManager.findOneByType(type, null);

		if (line != null) {
			PlatformRole platformRole = UserUtils.getUserPlatformRole();
			if (PlatformRole.Admin != platformRole) {
				if (line.getContent().startsWith("[")) {
					List<Map> list = JsonUtils.deserializeArray(line.getContent(), Map.class);
					list.forEach(map0 -> {
						List<Map> data = (List) map0.get("data");

						data = data.stream().filter(map -> {
							Object createdBy = map.get("createdBy");
							if (createdBy != null && !SecurityUtils.getUsername().equals(createdBy)) {
								return false;
							}
							return true;
						}).collect(Collectors.toList());

						map0.put("data", data);
					});

					line.setContent(JsonUtils.serialize(list));
				} else {
					//
				}
			}
		}

		return ResponseEntity.ok(line);
	}

	@PostMapping(value = "api/report/update")
	public ResponseEntity<AjaxResult> updateReport() {
		reportService.update();
		return ResponseEntity.ok(success());
	}
}

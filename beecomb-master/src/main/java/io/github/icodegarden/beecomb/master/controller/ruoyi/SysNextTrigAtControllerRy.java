package io.github.icodegarden.beecomb.master.controller.ruoyi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import io.github.icodegarden.beecomb.master.mapper.UpdateNextTrigAtMapper;
import io.github.icodegarden.beecomb.master.ruoyi.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Controller
public class SysNextTrigAtControllerRy extends BaseControllerRy {

	@Autowired
	private UpdateNextTrigAtMapper updateNextTrigAtMapper;
	
	@GetMapping("view/update_next_trig_at/delay")
	public String userCreate(HttpServletRequest request, ModelMap mmap) {
//		mmap.put("dict2", JsonSerialization.deserializeArray("[{\"dictLabel\":\"可用\",\"dictValue\":true},{\"dictLabel\":\"禁用\",\"dictValue\":false}]", Map.class));
		return "system/update_delay_next_trig_at";
	}

	@PostMapping(value = "api/update_next_trig_at/delay")
	public ResponseEntity<AjaxResult> createUser(@Validated UpdateNextTrigAt dto) {
		long count = updateNextTrigAtMapper.updateDelay();
		
		log.info("updateNextTrigAt delay count:{}",count);
		return ResponseEntity.ok(AjaxResult.success(count));
	}

	@Data
	public static class UpdateNextTrigAt{
		private Integer limit;
	}
}

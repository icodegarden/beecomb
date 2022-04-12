package io.github.icodegarden.beecomb.master.controller.ruoyi;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.backend.pojo.query.JobExecuteRecordQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobExecuteRecordVO;
import io.github.icodegarden.beecomb.master.manager.UserManager;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.PlatformRole;
import io.github.icodegarden.beecomb.master.pojo.query.UserQuery;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateUserDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdatePasswordDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdatePasswordNonOldDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdateUserDTO;
import io.github.icodegarden.beecomb.master.ruoyi.AjaxResult;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;
import io.github.icodegarden.beecomb.master.security.UserDetails;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Controller
public class ReportControllerRy extends BaseControllerRy {

	@Autowired
	private UserManager userService;

	@PostMapping(value = "api/report/list")
	public ResponseEntity<AjaxResult> listReports(@Validated CreateUserDTO dto) {
		try {
			userService.create(dto);
			return ResponseEntity.ok(success());
		} catch (IllegalArgumentException e) {
			/**
			 * 参数错误（包括唯一约束）400等
			 */
			return (ResponseEntity) ResponseEntity.status(400).body(e.getMessage());
		} catch (ErrorCodeException e) {
			/**
			 * 参数错误（包括唯一约束）400等
			 */
			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
		}
	}

	@PostMapping(value = "api/report/trig")
	public ResponseEntity<AjaxResult> trigReport(@Validated CreateUserDTO dto) {
		try {
			userService.create(dto);
			return ResponseEntity.ok(success());
		} catch (IllegalArgumentException e) {
			/**
			 * 参数错误（包括唯一约束）400等
			 */
			return (ResponseEntity) ResponseEntity.status(400).body(e.getMessage());
		} catch (ErrorCodeException e) {
			/**
			 * 参数错误（包括唯一约束）400等
			 */
			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
		}
	}
}

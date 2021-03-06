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

import io.github.icodegarden.beecomb.master.manager.UserManager;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.PlatformRole;
import io.github.icodegarden.beecomb.master.pojo.query.UserQuery;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.CreateUserApiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.UpdatePasswordApiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.UpdatePasswordNonOldApiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.api.UpdateUserApiDTO;
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
public class SysUserControllerRy extends BaseControllerRy {

	@Autowired
	private UserManager userService;

	@GetMapping("view/user/list")
	public String userList(HttpServletRequest request, ModelMap mmap) {
//		mmap.put("dict2", JsonSerialization.deserializeArray("[{\"dictLabel\":\"??????\",\"dictValue\":true},{\"dictLabel\":\"??????\",\"dictValue\":false}]", Map.class));
		return "system/user/list";
	}

	@PostMapping("api/user/list")
	public ResponseEntity<TableDataInfo> pageUsers(@RequestParam(required = false) String usernameLike,
			@RequestParam(required = false) String nameLike, @RequestParam(required = false) String phone,
			@RequestParam(required = false) Boolean actived, @RequestParam(required = false) PlatformRole platformRole,
			@RequestParam(defaultValue = "0") @Max(WebUtils.MAX_TOTAL_PAGES) int pageNum,
			@RequestParam(defaultValue = "10") @Max(WebUtils.MAX_PAGE_SIZE) int pageSize) {
		UserQuery query = UserQuery.builder().usernameLike(usernameLike).actived(actived).nameLike(nameLike)
				.phone(phone).platformRole(platformRole).page(pageNum).size(pageSize).sort("order by a.id desc")
				.build();
		query.setWith(UserQuery.With.builder().createdAt(true).createdBy(true).updatedAt(true).updatedBy(true).build());

		Page<UserPO> p = userService.page(query);
		return ResponseEntity.ok(getDataTable(p));
	}

	@GetMapping("view/user/create")
	public String userCreate(HttpServletRequest request, ModelMap mmap) {
//		mmap.put("dict2", JsonSerialization.deserializeArray("[{\"dictLabel\":\"??????\",\"dictValue\":true},{\"dictLabel\":\"??????\",\"dictValue\":false}]", Map.class));
		return "system/user/create";
	}

	@PostMapping(value = "api/user/create")
	public ResponseEntity<AjaxResult> createUser(@Validated CreateUserApiDTO dto) {
		try {
			userService.create(dto);
			return ResponseEntity.ok(success());
		} catch (IllegalArgumentException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(400).body(e.getMessage());
		} catch (ErrorCodeException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
		}
	}

	@GetMapping("view/user/{id}/update")
	public String userUpdate(HttpServletRequest request, ModelMap mmap, @PathVariable Long id) {
		UserPO user = userService.findOne(id, UserQuery.With.WITH_LEAST);
		mmap.addAttribute("user", user);
		return "system/user/update";
	}

	@PostMapping(value = "api/user/update")
	public ResponseEntity<AjaxResult> updateUser(@Validated UpdateUserApiDTO dto) {
		try {
			userService.update(dto);
			return ResponseEntity.ok(success());
		} catch (IllegalArgumentException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(400).body(e.getMessage());
		} catch (ErrorCodeException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
		}
	}

	@GetMapping("view/user/{id}/resetPwd")
	public String resetPwd(HttpServletRequest request, ModelMap mmap, @PathVariable Long id) {
		UserPO user = userService.findOne(id, UserQuery.With.WITH_LEAST);
		mmap.addAttribute("user", user);
		return "system/user/resetPwd";
	}

	@PostMapping("api/user/password")
	public ResponseEntity<AjaxResult> updatePassword(@Validated UpdatePasswordNonOldApiDTO dto) {
		try {
			userService.updatePassword(dto);
			return ResponseEntity.ok(success());
		} catch (IllegalArgumentException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(400).body(e.getMessage());
		} catch (ErrorCodeException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
		}
	}

	@PostMapping("api/user/{id}/enable")
	public ResponseEntity<AjaxResult> enableUser(@PathVariable Long id) {
		try {
			userService.enable(id);
			return ResponseEntity.ok(success());
		} catch (IllegalArgumentException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(400).body(e.getMessage());
		} catch (ErrorCodeException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
		}
	}

	@PostMapping("api/user/{id}/disable")
	public ResponseEntity<AjaxResult> disableUser(@PathVariable Long id) {
		try {
			userService.disable(id);
			return ResponseEntity.ok(success());
		} catch (IllegalArgumentException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(400).body(e.getMessage());
		} catch (ErrorCodeException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
		}
	}

	// -------------------------------------------------------------
	/**
	 * ??????????????????
	 */
	@GetMapping("view/user/profile/resetPwd")
	public String profileResetPwd(HttpServletRequest request, ModelMap mmap) {
		UserDetails userDetails = (UserDetails) SecurityUtils.getAuthenticatedUser();
		request.setAttribute("user", userDetails.getUser());
		return "system/user/profile/resetPwd";
	}

	@PostMapping("api/user/password/profile")
	public ResponseEntity<AjaxResult> updatePasswordProfile(@Validated UpdatePasswordApiDTO dto) {
		try {
			userService.updatePassword(dto);
			return ResponseEntity.ok(success());
		} catch (IllegalArgumentException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(400).body(e.getMessage());
		} catch (ErrorCodeException e) {
			/**
			 * ????????????????????????????????????400???
			 */
			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
		}
	}
}

package io.github.icodegarden.beecomb.master.controller.ruoyi;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ConcurrentModel;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;

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
import io.github.icodegarden.nursery.springboot.security.SecurityUtils;
import io.github.icodegarden.nutrient.lang.query.BaseQuery;
import io.github.icodegarden.nutrient.lang.spec.response.ErrorCodeException;
import jakarta.validation.constraints.Max;

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
	public String userList(ServerWebExchange exchange, ConcurrentModel mmap) {
//		mmap.put("dict2", JsonSerialization.deserializeArray("[{\"dictLabel\":\"可用\",\"dictValue\":true},{\"dictLabel\":\"禁用\",\"dictValue\":false}]", Map.class));
		return "system/user/list";
	}

	@PostMapping("api/user/list")
	public ResponseEntity<TableDataInfo> pageUsers(ServerWebExchange exchange
//			@RequestParam(required = false) String usernameLike,
//			@RequestParam(required = false) String nameLike, @RequestParam(required = false) String phone,
//			@RequestParam(required = false) Boolean actived, @RequestParam(required = false) PlatformRole platformRole,
//			@RequestParam(defaultValue = "0") @Max(BaseQuery.MAX_TOTAL_PAGES) int pageNum,
//			@RequestParam(defaultValue = "10") @Max(BaseQuery.MAX_PAGE_SIZE) int pageSize
	) {
		MultiValueMap<String, String> multiValueMap = getFormData(exchange);

		String usernameLike = StringUtils.hasText(multiValueMap.getFirst("usernameLike"))
				? multiValueMap.getFirst("usernameLike")
				: null;
		String nameLike = StringUtils.hasText(multiValueMap.getFirst("nameLike")) ? multiValueMap.getFirst("nameLike")
				: null;
		String phone = StringUtils.hasText(multiValueMap.getFirst("phone")) ? multiValueMap.getFirst("phone") : null;
		Boolean actived = StringUtils.hasText(multiValueMap.getFirst("actived"))
				? Boolean.valueOf(multiValueMap.getFirst("actived"))
				: null;
		PlatformRole platformRole = StringUtils.hasText(multiValueMap.getFirst("platformRole"))
				? PlatformRole.valueOf(multiValueMap.getFirst("platformRole"))
				: null;
		int pageNum = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageNum")).orElse("0"));
		int pageSize = Integer.parseInt(Optional.ofNullable(multiValueMap.getFirst("pageSize")).orElse("10"));

		UserQuery query = UserQuery.builder().usernameLike(usernameLike).actived(actived).nameLike(nameLike)
				.phone(phone).platformRole(platformRole).page(pageNum).size(pageSize).orderBy("a.id desc").build();
		query.setWith(UserQuery.With.builder().createdAt(true).createdBy(true).updatedAt(true).updatedBy(true).build());

		Page<UserPO> p = userService.page(query);
		return ResponseEntity.ok(getDataTable(p));
	}

	@GetMapping("view/user/create")
	public String userCreate(ServerWebExchange exchange, ConcurrentModel mmap) {
//		mmap.put("dict2", JsonSerialization.deserializeArray("[{\"dictLabel\":\"可用\",\"dictValue\":true},{\"dictLabel\":\"禁用\",\"dictValue\":false}]", Map.class));
		return "system/user/create";
	}

	@PostMapping(value = "api/user/create")
	public ResponseEntity<AjaxResult> createUser(ServerWebExchange exchange, @Validated CreateUserApiDTO dto) {
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

	@GetMapping("view/user/{id}/update")
	public String userUpdate(ServerWebExchange exchange, ConcurrentModel mmap, @PathVariable Long id) {
		UserPO user = userService.findOne(id, UserQuery.With.WITH_LEAST);
		mmap.put("user", user);
		return "system/user/update";
	}

	@PostMapping(value = "api/user/update")
	public ResponseEntity<AjaxResult> updateUser(ServerWebExchange exchange, @Validated UpdateUserApiDTO dto) {
		try {
			userService.update(dto);
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

	@GetMapping("view/user/{id}/resetPwd")
	public String resetPwd(ServerWebExchange exchange, ConcurrentModel mmap, @PathVariable Long id) {
		UserPO user = userService.findOne(id, UserQuery.With.WITH_LEAST);
		mmap.put("user", user);
		return "system/user/resetPwd";
	}

	@PostMapping("api/user/password")
	public ResponseEntity<AjaxResult> updatePassword(ServerWebExchange exchange,
			@Validated UpdatePasswordNonOldApiDTO dto) {
		try {
			userService.updatePassword(dto);
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

	@PostMapping("api/user/{id}/enable")
	public ResponseEntity<AjaxResult> enableUser(ServerWebExchange exchange, @PathVariable Long id) {
		try {
			userService.enable(id);
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

	@PostMapping("api/user/{id}/disable")
	public ResponseEntity<AjaxResult> disableUser(ServerWebExchange exchange, @PathVariable Long id) {
		try {
			userService.disable(id);
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

	// -------------------------------------------------------------
	/**
	 * 头部修改密码
	 */
	@GetMapping("view/user/profile/resetPwd")
	public String profileResetPwd(ServerWebExchange exchange, ConcurrentModel mmap) {
		UserDetails userDetails = (UserDetails) SecurityUtils.getAuthenticatedUser();
		exchange.getAttributes().put("user", userDetails.getUser());
		return "system/user/profile/resetPwd";
	}

	@PostMapping("api/user/password/profile")
	public ResponseEntity<AjaxResult> updatePasswordProfile(ServerWebExchange exchange,
			@Validated UpdatePasswordApiDTO dto) {
		try {
			userService.updatePassword(dto);
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

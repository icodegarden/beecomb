//package io.github.icodegarden.beecomb.master.controller;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import javax.validation.constraints.Max;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.github.pagehelper.Page;
//
//import io.github.icodegarden.beecomb.master.controller.ruoyi.BaseControllerRy;
//import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
//import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.PlatformRole;
//import io.github.icodegarden.beecomb.master.pojo.query.UserQuery;
//import io.github.icodegarden.beecomb.master.pojo.query.UserWith;
//import io.github.icodegarden.beecomb.master.pojo.transfer.CreateUserDTO;
//import io.github.icodegarden.beecomb.master.pojo.transfer.UpdatePasswordDTO;
//import io.github.icodegarden.beecomb.master.pojo.transfer.UpdateUserDTO;
//import io.github.icodegarden.beecomb.master.pojo.view.UserVO;
//import io.github.icodegarden.beecomb.master.service.UserService;
//import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
//import io.github.icodegarden.commons.springboot.web.util.WebUtils;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//@Validated
//@RestController
//public class UserController extends BaseControllerRy {
//
//	@Autowired
//	private UserService userService;
//
//	@PostMapping("api/v1/users")
//	public ResponseEntity<UserVO> createUser(@Validated @RequestBody CreateUserDTO dto) {
//		try {
//			UserPO user = userService.create(dto);
//
//			UserVO vo = new UserVO(user);
//
//			return ResponseEntity.ok(vo);
//		} catch (ErrorCodeException e) {
//			/**
//			 * 参数错误（包括唯一约束）400等
//			 */
//			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
//		}
//	}
//
//	@GetMapping("api/v1/users")
//	public ResponseEntity<List<UserVO>> pageUsers(@RequestParam(required = false) String usernameLike,
//			@RequestParam(required = false) String nameLike, @RequestParam(required = false) String phone,
//			@RequestParam(required = false) Boolean actived, @RequestParam(required = false) PlatformRole platformRole,
//			@RequestParam(defaultValue = "0") @Max(WebUtils.MAX_TOTAL_PAGES) int pageNum,
//			@RequestParam(defaultValue = "10") @Max(WebUtils.MAX_PAGE_SIZE) int pageSize) {
//		UserQuery query = UserQuery.builder().usernameLike(usernameLike).actived(actived).nameLike(nameLike).phone(phone)
//				.platformRole(platformRole).page(pageNum).size(pageSize).sort("order by a.id desc").build();
//		query.setWith(UserWith.builder().createdAt(true).createdBy(true).updatedAt(true).updatedBy(true).build());
//
//		Page<UserPO> p = userService.page(query);
//		
//		List<UserVO> list = p.getResult().stream().map(UserVO::new).collect(Collectors.toList());
//		return new ResponseEntity<List<UserVO>>(list, WebUtils.pageHeaders(p.getPages(), p.getTotal()), HttpStatus.OK);
//	}
//
//	@PutMapping("api/v1/users")
//	public ResponseEntity<Void> updateUser(@Validated @RequestBody UpdateUserDTO dto) {
//		try {
//			userService.update(dto);
//			return ResponseEntity.ok().build();
//		} catch (ErrorCodeException e) {
//			/**
//			 * 参数错误（包括唯一约束）400等
//			 */
//			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
//		}
//	}
//
//	@PutMapping("api/v1/users/password")
//	public ResponseEntity<Void> updatePassword(@Validated @RequestBody UpdatePasswordDTO dto) {
//		try {
//			userService.updatePassword(dto);
//			return ResponseEntity.ok().build();
//		} catch (ErrorCodeException e) {
//			/**
//			 * 参数错误（包括唯一约束）400等
//			 */
//			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
//		}
//	}
//
//	@PutMapping("api/v1/users/{id}/enable")
//	public ResponseEntity<Void> enableUser(@PathVariable Long id) {
//		try {
//			userService.enable(id);
//			return ResponseEntity.ok().build();
//		} catch (ErrorCodeException e) {
//			/**
//			 * 参数错误（包括唯一约束）400等
//			 */
//			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
//		}
//	}
//
//	@PutMapping("api/v1/users/{id}/disable")
//	public ResponseEntity<Void> disableUser(@PathVariable Long id) {
//		try {
//			userService.disable(id);
//			return ResponseEntity.ok().build();
//		} catch (ErrorCodeException e) {
//			/**
//			 * 参数错误（包括唯一约束）400等
//			 */
//			return (ResponseEntity) ResponseEntity.status(e.httpStatus()).body(e.getMessage());
//		}
//	}
//}

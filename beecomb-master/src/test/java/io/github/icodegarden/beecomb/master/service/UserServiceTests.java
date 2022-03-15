package io.github.icodegarden.beecomb.master.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.PlatformRole;
import io.github.icodegarden.beecomb.master.pojo.query.UserQuery;
import io.github.icodegarden.beecomb.master.pojo.query.UserWith;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateUserDTO;
import io.github.icodegarden.beecomb.master.security.AuthenticationBasedTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class UserServiceTests extends AuthenticationBasedTests {

	@Autowired
	UserService userService;

	UserPO create(String username) {
		CreateUserDTO dto = new CreateUserDTO();
		dto.setEmail("email");
		dto.setName("xff");
		dto.setPassword("password");
		dto.setPhone("13333333333");
		dto.setUsername(username);
		dto.setPlatformRole(PlatformRole.User);

		UserPO user = userService.create(dto);
		assertThat(user.getId()).isNotNull();

		return user;
	}

	@Test
	void page() {
		create("xff1");
		create("xff2");
		create("xff3");

		UserQuery query = UserQuery.builder().size(2)/* 每页2条 */.actived(true).nameLike("xf").phone("13333333333")
				.platformRole(PlatformRole.User).usernameLike("xf").build();
		Page<UserPO> page = userService.page(query);

		assertThat(page.getPages()).isEqualTo(2);
		assertThat(page.getTotal()).isEqualTo(3);
		assertThat(page.get(0).getUsername()).isEqualTo("xff1");

		query.setPage(2);// 下一页
		page = userService.page(query);
		assertThat(page.getPages()).isEqualTo(2);
		assertThat(page.getTotal()).isEqualTo(3);
		assertThat(page.get(0).getUsername()).isEqualTo("xff3");
	}

	@Test
	void enable_disable() {
		UserPO po = create("xff");

		userService.disable(po.getId());

		UserPO find = userService.findByUsername("xff", UserWith.WITH_MOST);
		assertThat(find.getActived()).isFalse();

		userService.enable(po.getId());

		find = userService.findByUsername("xff", UserWith.WITH_MOST);
		assertThat(find.getActived()).isTrue();
	}
}

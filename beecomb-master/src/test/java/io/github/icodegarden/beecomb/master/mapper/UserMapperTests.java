package io.github.icodegarden.beecomb.master.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.query.UserWith;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
class UserMapperTests {

	@Autowired
	UserMapper userMapper;

	UserPO create() {
		UserPO po = new UserPO();
		po.setActived(true);
		po.setCreatedAt(LocalDateTime.now());
		po.setCreatedBy("xff");
		po.setEmail("email");
		po.setName("xff");
		po.setPassword("aaaaaassssssssssddddddddd");
		po.setPhone("13333333333");
		po.setPlatformRole(UserPO.PlatformRole.Admin);
		po.setUpdatedAt(LocalDateTime.now());
		po.setUpdatedBy("xff");
		po.setUsername("username");
		userMapper.add(po);
		
		return po;
	}
	
	@Test
	void add() {
		UserPO po = create();
		
		assertThat(po.getId()).isNotNull();
	}

	@Test
	void findByUsername() {
		UserPO po = create();
		
		UserPO user = userMapper.findByUsername(po.getUsername(), UserWith.WITH_MOST);
		
		assertThat(user).isNotNull();
		assertThat(user.getId()).isEqualTo(po.getId());
		assertThat(user.getCreatedBy()).isNotNull();
		assertThat(user.getCreatedAt()).isNotNull();
		assertThat(user.getUpdatedBy()).isNotNull();
		assertThat(user.getUpdatedAt()).isNotNull();
	}
}

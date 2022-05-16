package io.github.icodegarden.beecomb.master.pojo.transfer.api;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.PlatformRole;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class CreateUserApiDTO {

	@Size(max = 24)
	@NotEmpty
	private String username;// varchar(24) NOT NULL COMMENT '用户名',
	@Size(max = 24)
	@NotEmpty
	private String password;// varchar(128) NOT NULL COMMENT '加密后的密码',
	@Size(max = 64)
	private String name;// varchar(64) COMMENT '姓名',
	@NotNull
	private PlatformRole platformRole;// varchar(20) NOT NULL COMMENT '平台角色:管理员、普通用户、...',
	@Size(max = 128)
	private String email;// varchar(128) COMMENT '邮箱地址',
	@Size(max = 32)
	private String phone;// varchar(32),
}

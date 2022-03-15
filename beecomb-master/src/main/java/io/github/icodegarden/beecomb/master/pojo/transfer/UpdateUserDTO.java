package io.github.icodegarden.beecomb.master.pojo.transfer;

import javax.validation.constraints.NotNull;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.PlatformRole;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UpdateUserDTO {

	@NotNull
	private Long id;// bigint unsigned NOT NULL AUTO_INCREMENT,
	private String name;// varchar(64) COMMENT '姓名',
	private String email;// varchar(128) COMMENT '邮箱地址',
	private String phone;// varchar(32),
	private Boolean actived;// bit(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
	private PlatformRole platformRole;// varchar(20) NOT NULL COMMENT '平台角色:管理员、普通用户、...',
}

package io.github.icodegarden.beecomb.master.pojo.view;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UserVO {

	private Long id;// bigint unsigned NOT NULL AUTO_INCREMENT,
	private String username;// varchar(24) NOT NULL COMMENT '用户名',
	private String name;// varchar(64) COMMENT '姓名',
	private String email;// varchar(128) COMMENT '邮箱地址',
	private String phone;// varchar(32),
	private Boolean actived;// bit(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
	private String platformRole;// varchar(20) NOT NULL COMMENT '平台角色:管理员、普通用户、...',
	private String createdBy;// varchar(32) NOT NULL COMMENT '创建人',
	private LocalDateTime createdAt;// timestamp NOT NULL COMMENT '创建时间',
	private String updatedBy;// varchar(32) NOT NULL COMMENT '最后修改人',
	private LocalDateTime updatedAt;// timestamp NOT NULL COMMENT '最后修改时间',

	public UserVO(UserPO po) {
		BeanUtils.copyProperties(po, this);
	}
}

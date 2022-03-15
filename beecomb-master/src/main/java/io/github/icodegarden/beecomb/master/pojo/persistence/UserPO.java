package io.github.icodegarden.beecomb.master.pojo.persistence;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UserPO {

	private Long id;// bigint unsigned NOT NULL AUTO_INCREMENT,
	private String username;// varchar(24) NOT NULL COMMENT '用户名',
	private String password;// varchar(128) NOT NULL COMMENT '加密后的密码',
	private String name;// varchar(64) COMMENT '姓名',
	private String email;// varchar(128) COMMENT '邮箱地址',
	private String phone;// varchar(32),
	private Boolean actived;// bit(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
	private PlatformRole platformRole;// varchar(20) NOT NULL COMMENT '平台角色:管理员、普通用户、...',
	private String createdBy;// varchar(32) NOT NULL COMMENT '创建人',
	private LocalDateTime createdAt;// timestamp NOT NULL COMMENT '创建时间',
	private String updatedBy;// varchar(32) NOT NULL COMMENT '最后修改人',
	private LocalDateTime updatedAt;// timestamp NOT NULL COMMENT '最后修改时间',

	public static enum PlatformRole {
		Admin, User
	}
	
	@Data
	public static class Update {
		private Long id;// bigint unsigned NOT NULL AUTO_INCREMENT,
		private String password;// varchar(128) NOT NULL COMMENT '加密后的密码',
		private String name;// varchar(64) COMMENT '姓名',
		private String email;// varchar(128) COMMENT '邮箱地址',
		private String phone;// varchar(32),
		private Boolean actived;// bit(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
		private PlatformRole platformRole;// varchar(20) NOT NULL COMMENT '平台角色:管理员、普通用户、...',
		private String updatedBy;// varchar(32) NOT NULL COMMENT '最后修改人',
		private LocalDateTime updatedAt;// timestamp NOT NULL COMMENT '最后修改时间',
		
		public Update(Long id) {
			this.id = id;
		}
		
	}
}

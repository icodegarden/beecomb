package io.github.icodegarden.beecomb.master.pojo.query;

import io.github.icodegarden.beecomb.common.pojo.query.BaseQuery;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.PlatformRole;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class UserQuery extends BaseQuery {

	private String usernameLike;// varchar(24) NOT NULL COMMENT '用户名',
	private String nameLike;// varchar(64) COMMENT '姓名',
	private String phone;// varchar(32),
	private Boolean actived;// bit(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
	private PlatformRole platformRole;// varchar(20) NOT NULL COMMENT '平台角色:管理员、普通用户、...',

	private With with;

	@Builder
	public UserQuery(int page, int size, String sort, String limit, String usernameLike, String nameLike, String phone,
			Boolean actived, PlatformRole platformRole, With with) {
		super(page, size, sort, limit);
		this.usernameLike = usernameLike;
		this.nameLike = nameLike;
		this.phone = phone;
		this.actived = actived;
		this.platformRole = platformRole;
		this.with = with;
	}

	@Builder
	@Data
	public static class With {

		public static final With WITH_LEAST = With.builder().build();
		public static final With WITH_MOST = With.builder().createdBy(true).createdAt(true).updatedBy(true)
				.updatedAt(true).build();

		private boolean createdBy;// varchar(32) NOT NULL COMMENT '创建人',
		private boolean createdAt;// timestamp NOT NULL COMMENT '创建时间',
		private boolean updatedBy;// varchar(32) NOT NULL COMMENT '最后修改人',
		private boolean updatedAt;// timestamp NOT NULL COMMENT '最后修改时间',

	}
}
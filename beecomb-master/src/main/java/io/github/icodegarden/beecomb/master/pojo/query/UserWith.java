package io.github.icodegarden.beecomb.master.pojo.query;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Fangfang.Xu
 *
 */
@Builder
@Data
public class UserWith {

	public static final UserWith WITH_LEAST = UserWith.builder().build();
	public static final UserWith WITH_MOST = UserWith.builder().createdBy(true).createdAt(true).updatedBy(true)
			.updatedAt(true).build();

	private boolean createdBy;// varchar(32) NOT NULL COMMENT '创建人',
	private boolean createdAt;// timestamp NOT NULL COMMENT '创建时间',
	private boolean updatedBy;// varchar(32) NOT NULL COMMENT '最后修改人',
	private boolean updatedAt;// timestamp NOT NULL COMMENT '最后修改时间',

}
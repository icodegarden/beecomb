package io.github.icodegarden.beecomb.master.pojo.persistence;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class ReportLinePO {

	private Long id;// bigint unsigned NOT NULL AUTO_INCREMENT,
	private String type;// varchar(24) NOT NULL COMMENT '用户名',
	private String content;// varchar(128) NOT NULL COMMENT '加密后的密码',
	private LocalDateTime updatedAt;// timestamp NOT NULL COMMENT '最后修改时间',

	@Setter
	@Getter
	public static class Update {
		private Long id;// bigint unsigned NOT NULL AUTO_INCREMENT,

		private String content;
		private LocalDateTime updatedAt;// timestamp NOT NULL COMMENT '最后修改时间',
	}
}

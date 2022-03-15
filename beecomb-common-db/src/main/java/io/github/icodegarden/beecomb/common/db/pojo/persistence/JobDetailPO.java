package io.github.icodegarden.beecomb.common.db.pojo.persistence;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class JobDetailPO {

	private Long jobId;// bigint NOT NULL,
	private String params;// TEXT comment '任务参数',
	private String desc;// varchar(200) comment '任务描述',
	
	@Builder
	@Data
	public static class Update{
		@NonNull
		private Long jobId;// bigint NOT NULL,
		private String params;// TEXT comment '任务参数',
		private String desc;// varchar(200) comment '任务描述',
	}
}

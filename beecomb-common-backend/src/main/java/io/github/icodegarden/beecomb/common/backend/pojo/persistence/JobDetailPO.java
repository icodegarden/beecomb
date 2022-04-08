package io.github.icodegarden.beecomb.common.backend.pojo.persistence;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

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
	
	@Setter
	@Getter
	@ToString
	public static class Update{
		@NonNull
		private Long jobId;// bigint NOT NULL,
		private String params;// TEXT comment '任务参数',
		private String desc;// varchar(200) comment '任务描述',
	}
}

package io.github.icodegarden.beecomb.common.db.pojo.transfer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Setter
@Getter
@ToString
public class CreateJobDetailDTO {
	
	private Long jobId;
	private String params;// TEXT comment '任务参数',
	private String desc;// varchar(200) comment '任务描述',
}

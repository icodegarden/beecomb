package io.github.icodegarden.beecomb.master.pojo.transfer;

import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UpdateJobDTO {

	private Long id;
	private Long uuid;
	
	@Size(max = 30)
	private String name;// varchar(30) NOT NULL,
	@Max(200) 
	private String desc;// varchar(200) comment '任务描述',

}

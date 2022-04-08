package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.Validateable;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UpdateJobDetailDTO implements Validateable {

	@NotNull
	private Long jobId;

	@Max(65535)
	private String params;// TEXT comment '任务参数',
	@Max(200)
	private String desc;// varchar(200) comment '任务描述',

	@Override
	public void validate() throws IllegalArgumentException {
		Assert.notNull(jobId, "Missing:jobId");
	}

}

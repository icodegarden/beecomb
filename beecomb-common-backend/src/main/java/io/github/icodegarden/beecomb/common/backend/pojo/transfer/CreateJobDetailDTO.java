package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.Validateable;
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
public class CreateJobDetailDTO implements Validateable {

	private Long jobId;
	private String params;// TEXT comment '任务参数',
	private String desc;// varchar(200) comment '任务描述',

	@Override
	public void validate() throws IllegalArgumentException {
		Assert.notNull(jobId, "Missing:jobId");
	}
}

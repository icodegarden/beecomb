package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

import java.util.Arrays;

import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.Validateable;
import io.github.icodegarden.beecomb.common.util.ClassUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
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
	@Max(65535)
	private String lastTrigResult;
	@Max(65535)
	private String lastExecuteReturns;
	
	@Override
	public void validate() throws IllegalArgumentException {
		Assert.notNull(jobId, "Missing:jobId");
	}

	@Override
	public boolean shouldUpdate() {
		return ClassUtils.anyFieldHasValue(this, Arrays.asList("jobId"));
	}
}

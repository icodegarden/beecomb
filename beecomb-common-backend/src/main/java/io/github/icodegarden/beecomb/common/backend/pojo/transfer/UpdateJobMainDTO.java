package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

import java.util.Arrays;

import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.Validateable;
import io.github.icodegarden.beecomb.common.constant.JobConstants;
import io.github.icodegarden.beecomb.common.util.ClassUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UpdateJobMainDTO implements Validateable {

	@NotNull
	private Long id;

	@Size(max = 30)
	private String name;// varchar(30) NOT NULL,
	@Size(max = 30)
	private String executorName;// varchar(30) NOT NULL,
	@Size(max = 30)
	private String jobHandlerName;// varchar(30) NOT NULL,
	@Min(1)
	@Max(10)
	private Integer priority;// tinyint NOT NULL default 5 comment '1-10仅当资源不足时起作用',
	@Min(1)
	@Max(5)
	private Integer weight;// tinyint NOT NULL default 1 comment '任务重量等级',
	@Min(2)
	@Max(64)
	private Integer maxParallelShards;
	private Boolean queued;// bit NOT NULL default 0,
	@Min(JobConstants.MIN_EXECUTE_TIMEOUT)
	@Max(JobConstants.MAX_EXECUTE_TIMEOUT)
	private Integer executeTimeout;// int NOT NULL default 10000 comment 'ms',
	/**
	 * 
	 */
	private Boolean nextTrigAtNull;
	private Boolean end;// bit NOT NULL default 0 comment '是否已结束',
	private String label;

	@Override
	public void validate() throws IllegalArgumentException {
		Assert.notNull(id, "Missing:id");
	}

	@Override
	public boolean shouldUpdate() {
		return ClassUtils.anyFieldHasValue(this, Arrays.asList("id"));
	}
}

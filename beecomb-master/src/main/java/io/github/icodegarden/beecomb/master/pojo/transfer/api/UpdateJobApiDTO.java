package io.github.icodegarden.beecomb.master.pojo.transfer.api;

import org.hibernate.validator.constraints.Length;
import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.Validateable;
import io.github.icodegarden.beecomb.common.constant.JobConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UpdateJobApiDTO implements Validateable {

	@NotNull
	private Long id;

	@Length(max = 30)
	private String name;// varchar(30) NOT NULL,
	@Length(max = 30)
	private String executorName;// varchar(30) NOT NULL,
	@Length(max = 30)
	private String jobHandlerName;// varchar(30) NOT NULL,
	@Min(1)
	@Max(10)
	private Integer priority;// tinyint NOT NULL default 5 comment '1-10仅当资源不足时起作用',
	@Min(1)
	@Max(Integer.MAX_VALUE)
	private Integer weight;// tinyint NOT NULL default 1 comment '任务重量等级',
	@Min(2)
	@Max(64)
	private Integer maxParallelShards;
	@Min(JobConstants.MIN_EXECUTE_TIMEOUT)
	@Max(JobConstants.MAX_EXECUTE_TIMEOUT)
	private Integer executeTimeout;// int NOT NULL default 10000 comment 'ms',
	@Length(max = 30)
	private String label;
	@Length(max = 65535)
	private String params;// TEXT comment '任务参数',
	@Length(max = 200)
	private String desc;// varchar(200) comment '任务描述',
	
	//-------------------------------
	@Max(JobConstants.MAX_EXECUTE_INTERVAL)
	private Long delay;
	private Integer retryOnExecuteFailed;
	private Integer retryBackoffOnExecuteFailed;
	private Integer retryOnNoQualified;
	private Integer retryBackoffOnNoQualified;
	
	//-------------------------------
	@Max(JobConstants.MAX_EXECUTE_INTERVAL)
	private Long scheduleFixRate;
	@Max(JobConstants.MAX_EXECUTE_INTERVAL)
	private Long scheduleFixDelay;
	private String sheduleCron;
	
	@Override
	public void validate() throws IllegalArgumentException {
		Assert.notNull(id, "Missing:id");
	}

}

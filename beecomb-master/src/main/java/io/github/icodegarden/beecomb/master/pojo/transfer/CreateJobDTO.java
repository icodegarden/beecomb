package io.github.icodegarden.beecomb.master.pojo.transfer;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.Validateable;
import io.github.icodegarden.beecomb.common.constant.JobConstants;
import io.github.icodegarden.beecomb.common.enums.JobType;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class CreateJobDTO implements Validateable {

	private String uuid;// varchar(64) UNIQUE comment '用户可以指定,默认null',
	@NotEmpty
	@Length(max = 30)
	private String name;// varchar(30) NOT NULL,
	@NotNull
	private JobType type;// tinyint NOT NULL comment '任务类型 0延时 1调度',
	@NotNull
	@Length(max = 30)
	private String executorName;// varchar(30) NOT NULL,
	@NotNull
	@Length(max = 30)
	private String jobHandlerName;// varchar(30) NOT NULL,
	@Min(1)
	@Max(10)
	private Integer priority;// tinyint NOT NULL default 5 comment '1-10仅当资源不足时起作用',
	@Min(1)
	@Max(5)
	private Integer weight;// tinyint NOT NULL default 1 comment '任务重量等级1-5',
	private Boolean parallel;
	@Min(2)
	@Max(64)
	private Integer maxParallelShards;
	@Min(JobConstants.MIN_EXECUTE_TIMEOUT)
	@Max(JobConstants.MAX_EXECUTE_TIMEOUT)
	private Integer executeTimeout;// int NOT NULL default 10000 comment 'ms',
	@Length(max = 65535)
	private String params;// TEXT comment '任务参数',
	@Length(max = 200)
	private String desc;// varchar(200) comment '任务描述',

	private Delay delay;
	private Schedule schedule;

	@Data
	public static class Delay {
		@NotNull
		@Max(JobConstants.MAX_EXECUTE_INTERVAL)
		private Long delay;
		private Integer retryOnExecuteFailed = 0;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败重试次数，包括连接失败、超时等',
		private Integer retryBackoffOnExecuteFailed = 3000;// int NOT NULL DEFAULT 3000 comment 'ms要求 gte 1000',
		private Integer retryOnNoQualified = 0;// smallint NOT NULL DEFAULT 0 comment '没有可用的executor时重试次数，包括不在线、超载时',
		private Integer retryBackoffOnNoQualified = 30000;// int NOT NULL DEFAULT 30000 comment 'ms要求 gte 5000',
	}

	@Data
	public static class Schedule {
		@Max(JobConstants.MAX_EXECUTE_INTERVAL)
		private Long scheduleFixRate;
		@Max(JobConstants.MAX_EXECUTE_INTERVAL)
		private Long scheduleFixDelay;
		private String sheduleCron;// varchar(20),
	}

	@Override
	public void validate() throws IllegalArgumentException {
		Assert.notNull(type, "Missing:type");
		if (type == JobType.Delay) {
			Assert.notNull(delay, "Missing:delay");
			Assert.notNull(delay.getDelay(), "Missing:delay.delay");
		}
		if (type == JobType.Schedule) {
			Assert.notNull(schedule, "Missing:schedule");
		}
	}

}

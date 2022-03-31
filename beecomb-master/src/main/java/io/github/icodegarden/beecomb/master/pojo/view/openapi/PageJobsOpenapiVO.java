package io.github.icodegarden.beecomb.master.pojo.view.openapi;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.master.pojo.view.JobVO;
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
public class PageJobsOpenapiVO {

	/**
	 * main
	 */
	private Long id;// bigint NOT NULL AUTO_INCREMENT,
	private String uuid;// varchar(64) UNIQUE comment '用户可以指定,默认null',
	private String name;// varchar(30) NOT NULL,
	private JobType type;// tinyint NOT NULL comment '任务类型 0延时 1调度',

	private String executorName;// varchar(30) NOT NULL,

	private String jobHandlerName;// varchar(30) NOT NULL,

	private Integer priority;// tinyint NOT NULL default 3 comment '1-5仅当资源不足时起作用',

	private Integer weight;// tinyint NOT NULL default 1 comment '任务重量等级1-5',

	private Boolean parallel;
	private Integer maxParallelShards;

	private Boolean queued;// bit NOT NULL default 0,
	private LocalDateTime queuedAt;// timestamp,
	private String queuedAtInstance;// varchar(21) comment 'ip:port',
	private LocalDateTime lastTrigAt;// timestamp,
	private String lastTrigResult;// varchar(200),
	private String lastExecuteExecutor;// varchar(21) comment 'ip:port',
	private String lastExecuteReturns;// varchar(200),
	private Boolean lastExecuteSuccess;// bit NOT NULL default 0,

	private Integer executeTimeout;// int comment 'ms',
	private LocalDateTime nextTrigAt;// timestamp comment '下次触发时间,初始是null',

	private Boolean end;// bit NOT NULL default 0 comment '是否已结束',
	private String createdBy;// varchar(30) comment '租户名',
	private LocalDateTime createdAt;// timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

	/**
	 * detail
	 */
	private String params;// TEXT comment '任务参数',
	private String desc;// varchar(200) comment '任务描述',

	/**
	 * delay
	 */
	private Integer delay;// int comment 'ms',
	private Integer retryOnExecuteFailed;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败重试次数，包括连接失败、超时等',
	private Integer retryBackoffOnExecuteFailed;// int NOT NULL DEFAULT 1000 comment 'ms要求 gte 1000',
	private Integer retriedTimesOnExecuteFailed;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败已重试次数',
	private Integer retryOnNoQualified;// smallint NOT NULL DEFAULT 0 comment '没有可用的executor时重试次数，包括不在线、超载时',
	private Integer retryBackoffOnNoQualified;// int NOT NULL DEFAULT 30000 comment 'ms要求 gte 5000',
	private Integer retriedTimesOnNoQualified;// smallint NOT NULL DEFAULT 0 comment '没有可用的executor时已重试次数',

	/**
	 * schedule
	 */
	private Integer scheduleFixRate;// int comment 'ms',
	private Integer scheduleFixDelay;// int comment 'ms',
	private String sheduleCron;// varchar(20),
	private Long scheduledTimes;// bigint,

	public static PageJobsOpenapiVO of(JobVO one) {
		if(one == null) {
			return null;
		}
		PageJobsOpenapiVO vo = new PageJobsOpenapiVO();
		BeanUtils.copyProperties(one, vo);
		return vo;
	}

}

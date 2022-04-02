package io.github.icodegarden.beecomb.common.db.pojo.view;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.db.pojo.data.JobMainDO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class JobMainVO {

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

	private JobDetailVO jobDetail;
	private DelayJobVO delayJob;
	private ScheduleJobVO scheduleJob;

	public static JobMainVO of(JobMainDO one) {
		if (one == null) {
			return null;
		}
		JobMainVO vo = new JobMainVO();

		BeanUtils.copyProperties(one, vo);

		vo.setJobDetail(JobDetailVO.of(one.getJobDetail()));
		vo.setDelayJob(DelayJobVO.of(one.getDelayJob()));
		vo.setScheduleJob(ScheduleJobVO.of(one.getScheduleJob()));

		return vo;
	}
}
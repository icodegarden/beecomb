package io.github.icodegarden.beecomb.common.db.pojo.persistence;

import java.time.LocalDateTime;

import io.github.icodegarden.beecomb.common.enums.JobType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;


/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class JobMainPO {

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
	
	@Getter
	@ToString
	public static class Update{
		@NonNull
		private Long id;// bigint NOT NULL AUTO_INCREMENT,
		private String name;// varchar(30) NOT NULL,
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
		
		/**
		 * -----------------------------------------------------
		 */
		private boolean nextTrigAtNull;
		
		@Builder
		private Update(@NonNull Long id, String name, Integer priority, Integer weight, Boolean queued,
				LocalDateTime queuedAt, String queuedAtInstance, LocalDateTime lastTrigAt, String lastTrigResult,
				String lastExecuteExecutor, String lastExecuteReturns, Boolean lastExecuteSuccess,
				Integer executeTimeout, LocalDateTime nextTrigAt, Boolean end, boolean nextTrigAtNull) {
			this.id = id;
			this.name = name;
			this.priority = priority;
			this.weight = weight;
			this.queued = queued;
			this.queuedAt = queuedAt;
			this.queuedAtInstance = queuedAtInstance;
			this.lastTrigAt = lastTrigAt;
			this.lastTrigResult = lastTrigResult;
			this.lastExecuteExecutor = lastExecuteExecutor;
			this.lastExecuteReturns = lastExecuteReturns;
			this.lastExecuteSuccess = lastExecuteSuccess;
			this.executeTimeout = executeTimeout;
			this.nextTrigAt = nextTrigAt;
			this.nextTrigAtNull = nextTrigAtNull;

			if(end != null) {
				setEnd(end);
			}
		}
		
		/**
		 * 
		 * @param end 当end true时，自动处理与其相关的字段更新
		 */
		public void setEnd(boolean end) {
			this.end = end;
			if(end) {
				queued = false;
				nextTrigAtNull = true;
			}
		}
	}
}

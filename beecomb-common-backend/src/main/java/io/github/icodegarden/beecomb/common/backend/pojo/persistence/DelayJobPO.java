package io.github.icodegarden.beecomb.common.backend.pojo.persistence;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.icodegarden.beecomb.common.pojo.biz.DelayBO;
import lombok.Builder;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class DelayJobPO {

	private Long jobId;// bigint NOT NULL,
	private Integer delay;// int comment 'ms',
	private Integer retryOnExecuteFailed;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败重试次数，包括连接失败、超时等',
	private Integer retryBackoffOnExecuteFailed;// int NOT NULL DEFAULT 1000 comment 'ms要求 gte 1000',
	private Integer retriedTimesOnExecuteFailed;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败已重试次数',
	private Integer retryOnNoQualified;// smallint NOT NULL DEFAULT 0 comment '没有可用的executor时重试次数，包括不在线、超载时',
	private Integer retryBackoffOnNoQualified;// int NOT NULL DEFAULT 30000 comment 'ms要求 gte 5000',
	private Integer retriedTimesOnNoQualified;// smallint NOT NULL DEFAULT 0 comment '没有可用的executor时已重试次数',

	@JsonIgnore
	public DelayBO toDelayBO() {
		DelayBO delay = new DelayBO();
		BeanUtils.copyProperties(this, delay);
		return delay;
	}
	
	@Builder
	@Data
	public static class Update {
		private Long jobId;// bigint NOT NULL,
		private Integer retryOnExecuteFailed;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败重试次数，包括连接失败、超时等',
		private Integer retryBackoffOnExecuteFailed;// int NOT NULL DEFAULT 1000 comment 'ms要求 gte 1000',
		private Integer retriedTimesOnExecuteFailed;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败已重试次数',
		private Integer retryOnNoQualified;// smallint NOT NULL DEFAULT 0 comment '没有合格的executor时重试次数，包括不在线、超载时',
		private Integer retryBackoffOnNoQualified;// int NOT NULL DEFAULT 30000 comment 'ms要求 gte 5000',
		private Integer retriedTimesOnNoQualified;// smallint NOT NULL DEFAULT 0 comment '没有合格的executor时已重试次数',
	}
}

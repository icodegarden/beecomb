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
public class CreateDelayJobDTO implements Validateable {

	private Long jobId;
	private Long delay;// int comment 'ms',
	private Integer retryOnExecuteFailed = 0;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败重试次数，包括连接失败、超时等',
	private Integer retryBackoffOnExecuteFailed = 3000;// int NOT NULL DEFAULT 3000 comment 'ms要求 gte 1000',
	private Integer retryOnNoQualified = 0;// smallint NOT NULL DEFAULT 0 comment '没有可用的executor时重试次数，包括不在线、超载时',
	private Integer retryBackoffOnNoQualified = 30000;// int NOT NULL DEFAULT 30000 comment 'ms要求 gte 5000',

	@Override
	public void validate() throws IllegalArgumentException {
		Assert.notNull(getDelay(), "Missing:delay");
	}

}

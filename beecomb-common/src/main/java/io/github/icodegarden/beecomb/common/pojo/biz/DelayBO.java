package io.github.icodegarden.beecomb.common.pojo.biz;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.icodegarden.beecomb.common.executor.DelayJob;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;
/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DelayBO implements Serializable {
	private static final long serialVersionUID = -7494615898068876183L;

	private Long delay;
	private Integer retryOnExecuteFailed;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败重试次数，包括连接失败、超时等',
	private Integer retryBackoffOnExecuteFailed;// int NOT NULL DEFAULT 1000 comment 'ms要求 gte 1000',
	private Integer retriedTimesOnExecuteFailed;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败已重试次数',
	private Integer retryOnNoQualified;// smallint NOT NULL DEFAULT 0 comment '没有合格的executor时重试次数，包括不在线、超载时',
	private Integer retryBackoffOnNoQualified;// int NOT NULL DEFAULT 30000 comment 'ms要求 gte 5000',
	private Integer retriedTimesOnNoQualified;// smallint NOT NULL DEFAULT 0 comment '没有合格的executor时已重试次数',

	/**
	 * 每秒执行次数
	 * 
	 * @return
	 */
	public double rateOfSecond() {
		return 1000.0 / getDelay();
	}
	
	/**
	 * 计算出OnNoQualified时,下次的触发时间<br>
	 * 时间=当前 + retryBackoffOnNoQualified<br>
	 * 
	 * @return
	 */
	@JsonIgnore
	public LocalDateTime calcNextTrigAtOnNoQualified() {
		Integer delayMillis = getRetryBackoffOnNoQualified();
		return SystemUtils.now().plus(delayMillis, ChronoUnit.MILLIS);
	}

	/**
	 * OnNoQualified时距离下次触发的延迟毫秒<br>
	 * 时间=retryBackoffOnNoQualified<br>
	 * 
	 * @return
	 */
	@JsonIgnore
	public long calcNextTrigDelayMillisOnNoQualified() {
		return getRetryBackoffOnNoQualified();
	}

	/**
	 * 计算出OnExecuteFailed时,下次的触发时间<br>
	 * 时间=当前 + retryBackoffOnExecuteFailed<br>
	 * 
	 * @return
	 */
	@JsonIgnore
	public LocalDateTime calcNextTrigAtOnExecuteFailed() {
		Integer delayMillis = getRetryBackoffOnExecuteFailed();
		return SystemUtils.now().plus(delayMillis, ChronoUnit.MILLIS);
	}

	/**
	 * OnExecuteFailed时距离下次触发的延迟毫秒<br>
	 * 时间=retryBackoffOnExecuteFailed<br>
	 * 
	 * @return
	 */
	@JsonIgnore
	public long calcNextTrigDelayMillisOnExecuteFailed() {
		return getRetryBackoffOnExecuteFailed();
	}
	
	public static DelayBO of(DelayJob delayJob) {
		DelayBO delay = new DelayBO();
		BeanUtils.copyProperties(delayJob, delay);
		return delay;
	}

	public Long getDelay() {
		return delay;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	public Integer getRetryOnExecuteFailed() {
		return retryOnExecuteFailed;
	}

	public void setRetryOnExecuteFailed(Integer retryOnExecuteFailed) {
		this.retryOnExecuteFailed = retryOnExecuteFailed;
	}

	public Integer getRetryBackoffOnExecuteFailed() {
		return retryBackoffOnExecuteFailed;
	}

	public void setRetryBackoffOnExecuteFailed(Integer retryBackoffOnExecuteFailed) {
		this.retryBackoffOnExecuteFailed = retryBackoffOnExecuteFailed;
	}

	public Integer getRetriedTimesOnExecuteFailed() {
		return retriedTimesOnExecuteFailed;
	}

	public void setRetriedTimesOnExecuteFailed(Integer retriedTimesOnExecuteFailed) {
		this.retriedTimesOnExecuteFailed = retriedTimesOnExecuteFailed;
	}

	public Integer getRetryOnNoQualified() {
		return retryOnNoQualified;
	}

	public void setRetryOnNoQualified(Integer retryOnNoQualified) {
		this.retryOnNoQualified = retryOnNoQualified;
	}

	public Integer getRetryBackoffOnNoQualified() {
		return retryBackoffOnNoQualified;
	}

	public void setRetryBackoffOnNoQualified(Integer retryBackoffOnNoQualified) {
		this.retryBackoffOnNoQualified = retryBackoffOnNoQualified;
	}

	public Integer getRetriedTimesOnNoQualified() {
		return retriedTimesOnNoQualified;
	}

	public void setRetriedTimesOnNoQualified(Integer retriedTimesOnNoQualified) {
		this.retriedTimesOnNoQualified = retriedTimesOnNoQualified;
	}

	@Override
	public String toString() {
		return "DelayBO [delay=" + delay + ", retryOnExecuteFailed=" + retryOnExecuteFailed
				+ ", retryBackoffOnExecuteFailed=" + retryBackoffOnExecuteFailed + ", retriedTimesOnExecuteFailed="
				+ retriedTimesOnExecuteFailed + ", retryOnNoQualified=" + retryOnNoQualified
				+ ", retryBackoffOnNoQualified=" + retryBackoffOnNoQualified + ", retriedTimesOnNoQualified="
				+ retriedTimesOnNoQualified + "]";
	}
	
}
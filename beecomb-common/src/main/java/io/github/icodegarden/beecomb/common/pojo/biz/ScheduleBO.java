package io.github.icodegarden.beecomb.common.pojo.biz;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.icodegarden.beecomb.common.executor.ScheduleJob;
import io.github.icodegarden.commons.lang.util.CronUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ScheduleBO implements Serializable {
	private static final long serialVersionUID = 110264587448091798L;
	
	private Integer scheduleFixRate;// int comment 'ms',
	private Integer scheduleFixDelay;// int comment 'ms',
	private String sheduleCron;// varchar(20),
	private Long scheduledTimes;// bigint,

	/**
	 * 每秒执行次数
	 * 
	 * @return
	 */
	public double rateOfSecond() {
		if (getScheduleFixDelay() != null) {
			return 1000.0 / getScheduleFixDelay();
		}
		if (getScheduleFixRate() != null) {
			return 1000.0 / getScheduleFixRate();
		}
		long ms = CronUtils.betweenMillis(getSheduleCron());
		return 1000.0 / ms;
	}
	
	/**
	 * 计算出下一次任务触发时间<br>
	 * <h1>只适用于OnEnQueue</h1>
	 * 
	 * scheduleFixRate,scheduleFixDelay 时间=now + delay;sheduleCron按真实的cron进行计算<br>
	 * 
	 * @return
	 */
	@JsonIgnore
	public LocalDateTime calcNextTrigAtOnEnQueue() {
		if (getScheduleFixRate() != null) {
			return SystemUtils.now().plus(getScheduleFixRate(), ChronoUnit.MILLIS);
		}
		if (getScheduleFixDelay() != null) {
			return SystemUtils.now().plus(getScheduleFixDelay(), ChronoUnit.MILLIS);
		}
		if (getSheduleCron() != null) {
			return CronUtils.next(getSheduleCron());
		}
		throw new IllegalArgumentException("required param not found in schedule");
	}

	/**
	 * 计算出距离下一次任务触发的毫秒<br>
	 * <h1>只适用于OnEnQueue</h1>
	 * 
	 * @return
	 */
	@JsonIgnore
	public long calcNextTrigDelayMillisOnEnQueue() {
		LocalDateTime next = calcNextTrigAtOnEnQueue();
		Duration between = Duration.between(SystemUtils.now(), next);
		return between.toMillis();
	}

	/**
	 * 基于已触发后，计算出下一次任务触发时间<br>
	 * 
	 * scheduleFixRate：时间=start + scheduleFixRate<br>
	 * scheduleFixDelay：时间=end + scheduleFixDelay<br>
	 * sheduleCron：时间=按真实的cron进行计算<br>
	 * 
	 * @param start NotNull,已触发开始的时间
	 * @param end   NotNull,已触发结束的时间
	 * @return
	 */
	@JsonIgnore
	public LocalDateTime calcNextTrigAtOnTriggered(LocalDateTime start, LocalDateTime end) {
		if (getScheduleFixRate() != null) {
			return start.plus(getScheduleFixRate(), ChronoUnit.MILLIS);
		}
		if (getScheduleFixDelay() != null) {
			return end.plus(getScheduleFixDelay(), ChronoUnit.MILLIS);
		}
		if (getSheduleCron() != null) {
			return CronUtils.next(getSheduleCron());
		}
		throw new IllegalArgumentException("required param not found in schedule");
	}

	/**
	 * 基于已触发后，计算出距离下一次任务触发的毫秒<br>
	 * 
	 * @return
	 */
	@JsonIgnore
	public long calcNextTrigDelayMillis(LocalDateTime start, LocalDateTime end) {
		LocalDateTime next = calcNextTrigAtOnTriggered(start, end);
		Duration between = Duration.between(SystemUtils.now(), next);
		return between.toMillis();
	}
	
	public static ScheduleBO of(ScheduleJob scheduleJob) {
		ScheduleBO schedule = new ScheduleBO();
		BeanUtils.copyProperties(scheduleJob, schedule);
		return schedule;
	}

	public Integer getScheduleFixRate() {
		return scheduleFixRate;
	}

	public void setScheduleFixRate(Integer scheduleFixRate) {
		this.scheduleFixRate = scheduleFixRate;
	}

	public Integer getScheduleFixDelay() {
		return scheduleFixDelay;
	}

	public void setScheduleFixDelay(Integer scheduleFixDelay) {
		this.scheduleFixDelay = scheduleFixDelay;
	}

	public String getSheduleCron() {
		return sheduleCron;
	}

	public void setSheduleCron(String sheduleCron) {
		this.sheduleCron = sheduleCron;
	}

	public Long getScheduledTimes() {
		return scheduledTimes;
	}

	public void setScheduledTimes(Long scheduledTimes) {
		this.scheduledTimes = scheduledTimes;
	}

	@Override
	public String toString() {
		return "ScheduleBO [scheduleFixRate=" + scheduleFixRate + ", scheduleFixDelay=" + scheduleFixDelay
				+ ", sheduleCron=" + sheduleCron + ", scheduledTimes=" + scheduledTimes + "]";
	}
	
}
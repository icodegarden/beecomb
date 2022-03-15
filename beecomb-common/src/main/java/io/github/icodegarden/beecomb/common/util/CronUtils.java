//package io.github.icodegarden.beecomb.common.util;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//
//import org.springframework.scheduling.support.CronExpression;
//
//import io.github.icodegarden.commons.lang.annotation.Nullable;
//import io.github.icodegarden.commons.lang.util.SystemClock;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//public abstract class CronUtils {
//
//	public static boolean isValid(@Nullable String cron) {
//		return CronExpression.isValidExpression(cron);
//	}
//
//	/**
//	 * 得出下次 - 下下次 之间的时间差
//	 * 
//	 * @param cron
//	 * @return
//	 */
//	public static long betweenMillis(String cron) {
//		CronExpression cronExpression = CronExpression.parse(cron);
//		LocalDateTime now = SystemClock.now();
//		LocalDateTime next = cronExpression.next(now);
//		LocalDateTime next_next = cronExpression.next(next);
//		Duration between = Duration.between(next, next_next);
//		return between.toMillis();
//	}
//
//	/**
//	 * 计算出距离下次执行的延迟毫秒
//	 * 
//	 * @param cron
//	 * @return
//	 */
//	public static long nextDelayMillis(String cron) {
//		LocalDateTime now = SystemClock.now();
//		LocalDateTime next = next(cron);
//		Duration between = Duration.between(now, next);
//		return between.toMillis();
//	}
//	
//	/**
//	 * 下次的执行时间
//	 * @param cron
//	 * @return
//	 */
//	public static LocalDateTime next(String cron) {
//		CronExpression cronExpression = CronExpression.parse(cron);
//		LocalDateTime now = SystemClock.now();
//		return cronExpression.next(now);
//	}
//}

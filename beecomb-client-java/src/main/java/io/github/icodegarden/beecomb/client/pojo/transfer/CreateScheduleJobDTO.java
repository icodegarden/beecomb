package io.github.icodegarden.beecomb.client.pojo.transfer;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.nutrient.lang.annotation.NotNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class CreateScheduleJobDTO extends CreateJobDTO {

	@NotNull
	private Schedule schedule;

	public CreateScheduleJobDTO(String name, String executorName, String jobHandlerName, Schedule schedule) {
		super(name, JobType.Schedule, executorName, jobHandlerName);
		this.schedule = schedule;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	@Override
	public String toString() {
		return "ScheduleJob [schedule=" + schedule + ", toString()=" + super.toString() + "]";
	}

	/**
	 * scheduleFixRate、scheduleFixDelay、sheduleCron必选其一
	 * @author Fangfang.Xu
	 *
	 */
	public static class Schedule {
		/**
		 * 任务执行FixRate时间毫秒，1000-31536000000000
		 */
		private Long scheduleFixRate;
		/**
		 * 任务执行FixDelay时间毫秒，1000-31536000000000
		 */
		private Long scheduleFixDelay;
		/**
		 * 任务cron，例如 0 0/2 * * * *
		 */
		private String sheduleCron;

		private Schedule() {
		}

		public static Schedule scheduleFixRate(long scheduleFixRate) {
			Schedule schedule = new Schedule();
			schedule.scheduleFixRate = scheduleFixRate;
			return schedule;
		}

		public static Schedule scheduleFixDelay(long scheduleFixDelay) {
			Schedule schedule = new Schedule();
			schedule.scheduleFixDelay = scheduleFixDelay;
			return schedule;
		}

		public static Schedule sheduleCron(String sheduleCron) {
			Schedule schedule = new Schedule();
			schedule.sheduleCron = sheduleCron;
			return schedule;
		}

		public Long getScheduleFixRate() {
			return scheduleFixRate;
		}

		public Long getScheduleFixDelay() {
			return scheduleFixDelay;
		}

		public String getSheduleCron() {
			return sheduleCron;
		}

		@Override
		public String toString() {
			return "Schedule [scheduleFixRate=" + scheduleFixRate + ", scheduleFixDelay=" + scheduleFixDelay
					+ ", sheduleCron=" + sheduleCron + "]";
		}

	}

}

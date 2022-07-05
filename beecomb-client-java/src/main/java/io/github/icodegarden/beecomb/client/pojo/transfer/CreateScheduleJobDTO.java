package io.github.icodegarden.beecomb.client.pojo.transfer;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.commons.lang.annotation.NotNull;

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

	public static class Schedule {
		private Long scheduleFixRate;
		private Long scheduleFixDelay;
		private String sheduleCron;

		private Schedule() {
		}

		public static Schedule scheduleFixRate(Long scheduleFixRate) {
			Schedule schedule = new Schedule();
			schedule.scheduleFixRate = scheduleFixRate;
			return schedule;
		}

		public static Schedule scheduleFixDelay(Long scheduleFixDelay) {
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

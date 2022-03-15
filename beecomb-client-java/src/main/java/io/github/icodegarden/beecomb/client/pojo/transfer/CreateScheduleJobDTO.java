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
	private CreateScheduleJobDTO schedule;

	public CreateScheduleJobDTO(String name, String executorName, String jobHandlerName, CreateScheduleJobDTO schedule) {
		super(name, JobType.Schedule, executorName, jobHandlerName);
		this.schedule = schedule;
	}

	public CreateScheduleJobDTO getSchedule() {
		return schedule;
	}

	public void setSchedule(CreateScheduleJobDTO schedule) {
		this.schedule = schedule;
	}

	@Override
	public String toString() {
		return "ScheduleJob [schedule=" + schedule + ", toString()=" + super.toString() + "]";
	}

	public static class Schedule {
		private Integer scheduleFixRate;
		private Integer scheduleFixDelay;
		private String sheduleCron;

		private Schedule() {
		}

		public static Schedule scheduleFixRate(Integer scheduleFixRate) {
			Schedule schedule = new Schedule();
			schedule.scheduleFixRate = scheduleFixRate;
			return schedule;
		}

		public static Schedule scheduleFixDelay(Integer scheduleFixDelay) {
			Schedule schedule = new Schedule();
			schedule.scheduleFixDelay = scheduleFixDelay;
			return schedule;
		}

		public static Schedule sheduleCron(String sheduleCron) {
			Schedule schedule = new Schedule();
			schedule.sheduleCron = sheduleCron;
			return schedule;
		}

		public Integer getScheduleFixRate() {
			return scheduleFixRate;
		}

		public Integer getScheduleFixDelay() {
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

package io.github.icodegarden.beecomb.client.pojo.transfer;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.commons.lang.annotation.NotNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class CreateDelayJobDTO extends CreateJobDTO {

	@NotNull
	private Delay delay;

	public CreateDelayJobDTO(String name, String executorName, String jobHandlerName, Delay delay) {
		super(name, JobType.Delay, executorName, jobHandlerName);
		this.delay = delay;
	}

	public Delay getDelay() {
		return delay;
	}

	public void setDelay(Delay delay) {
		this.delay = delay;
	}

	@Override
	public String toString() {
		return "DelayJob [delay=" + delay + ", toString()=" + super.toString() + "]";
	}

	public static class Delay {
		/**
		 * 任务的延迟执行时间毫秒，1000-31536000000000
		 */
		@NotNull
		private Long delay;
		/**
		 * 当任务执行失败时重试次数，默认0
		 */
		private Integer retryOnExecuteFailed;
		/**
		 * 重试回退时间毫秒，默认3000
		 */
		private Integer retryBackoffOnExecuteFailed;
		/**
		 * 当任务执行没有合格的Executor时重试次数，默认0
		 */
		private Integer retryOnNoQualified;
		/**
		 * 重试回退时间毫秒，默认30000
		 */
		private Integer retryBackoffOnNoQualified;

		public Delay(Long delay) {
			this.delay = delay;
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

		@Override
		public String toString() {
			return "Delay [delay=" + delay + ", retryOnExecuteFailed=" + retryOnExecuteFailed
					+ ", retryBackoffOnExecuteFailed=" + retryBackoffOnExecuteFailed + ", retryOnNoQualified="
					+ retryOnNoQualified + ", retryBackoffOnNoQualified=" + retryBackoffOnNoQualified + "]";
		}
	}

}

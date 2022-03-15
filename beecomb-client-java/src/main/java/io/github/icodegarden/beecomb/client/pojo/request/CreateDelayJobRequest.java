package io.github.icodegarden.beecomb.client.pojo.request;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.commons.lang.annotation.NotNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class CreateDelayJobRequest extends CreateJobRequest {

	@NotNull
	private Delay delay;

	public CreateDelayJobRequest(String name, String executorName, String jobHandlerName, Delay delay) {
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
		@NotNull
		private Integer delay;
		private Integer retryOnExecuteFailed;
		private Integer retryBackoffOnExecuteFailed;
		private Integer retryOnNoQualified;
		private Integer retryBackoffOnNoQualified;

		public Delay(Integer delay) {
			this.delay = delay;
		}

		public Integer getDelay() {
			return delay;
		}

		public void setDelay(Integer delay) {
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

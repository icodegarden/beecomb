package io.github.icodegarden.beecomb.common.executor;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JobHandlerRegistrationBean implements Serializable {
	private static final long serialVersionUID = 5078420176605232558L;

	private String executorName;
	private Set<JobHandlerRegistration> jobHandlerRegistrations;

	public String getExecutorName() {
		return executorName;
	}

	public void setExecutorName(String executorName) {
		this.executorName = executorName;
	}

	public Set<JobHandlerRegistration> getJobHandlerRegistrations() {
		return jobHandlerRegistrations;
	}

	public void setJobHandlerRegistrations(Set<JobHandlerRegistration> jobHandlerRegistrations) {
		this.jobHandlerRegistrations = jobHandlerRegistrations;
	}

	@Override
	public int hashCode() {
		return Objects.hash(executorName, jobHandlerRegistrations);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobHandlerRegistrationBean other = (JobHandlerRegistrationBean) obj;
		return Objects.equals(executorName, other.executorName)
				&& Objects.equals(jobHandlerRegistrations, other.jobHandlerRegistrations);
	}

	@Override
	public String toString() {
		return "JobHandlerRegistrationBean [executorName=" + executorName + ", jobHandlerRegistrations="
				+ jobHandlerRegistrations + "]";
	}

	public static class JobHandlerRegistration implements Serializable {
		private static final long serialVersionUID = 7631917993591060274L;

		private String jobHandlerName;

		public String getJobHandlerName() {
			return jobHandlerName;
		}

		public void setJobHandlerName(String jobHandlerName) {
			this.jobHandlerName = jobHandlerName;
		}

		@Override
		public int hashCode() {
			return Objects.hash(jobHandlerName);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JobHandlerRegistration other = (JobHandlerRegistration) obj;
			return Objects.equals(jobHandlerName, other.jobHandlerName);
		}

		@Override
		public String toString() {
			return "JobHandlerRegistration [jobHandlerName=" + jobHandlerName + "]";
		}

	}
}
package io.github.icodegarden.beecomb.common.executor;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import io.github.icodegarden.nutrient.lang.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@EqualsAndHashCode
@ToString
@Setter
@Getter
public class JobHandlerRegistrationBean implements Serializable {
	private static final long serialVersionUID = 5078420176605232558L;

	@Nullable
	private String namespace;
	
	private String executorName;
	
	private Set<JobHandlerRegistration> jobHandlerRegistrations;

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
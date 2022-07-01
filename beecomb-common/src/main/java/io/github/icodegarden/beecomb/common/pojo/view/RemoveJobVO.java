package io.github.icodegarden.beecomb.common.pojo.view;

import java.io.Serializable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RemoveJobVO implements Serializable {
	private static final long serialVersionUID = 7987214679035546483L;

	private Long jobId;
	/**
	 * 是否移除成功
	 */
	private Boolean removed;

	public RemoveJobVO() {
	}

	public RemoveJobVO(Long jobId, Boolean removed) {
		this.jobId = jobId;
		this.removed = removed;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public Boolean getRemoved() {
		return removed;
	}

	public void setRemoved(Boolean removed) {
		this.removed = removed;
	}

	@Override
	public String toString() {
		return "RemoveJobVO [jobId=" + jobId + ", removed=" + removed + "]";
	}
}

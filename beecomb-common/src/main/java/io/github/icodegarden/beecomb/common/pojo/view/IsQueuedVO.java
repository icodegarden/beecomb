package io.github.icodegarden.beecomb.common.pojo.view;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class IsQueuedVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long jobId;
	/**
	 * 是否在队列中
	 */
	private Boolean queued;

	public IsQueuedVO() {
	}

	public IsQueuedVO(Long jobId, Boolean queued) {
		this.jobId = jobId;
		this.queued = queued;
	}

}

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
public class RunJobVO implements Serializable {
	private static final long serialVersionUID = 7987214679035546483L;

	private Long jobId;
	/**
	 * 是否成功
	 */
	private Boolean success;

	public RunJobVO() {
	}

	public RunJobVO(Long jobId, Boolean success) {
		this.jobId = jobId;
		this.success = success;
	}
}

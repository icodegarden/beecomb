package io.github.icodegarden.beecomb.master.pojo.view.openapi;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Setter
@Getter
@ToString
public class DeleteJobOpenapiVO {

	private final Long jobId;
	private final Boolean Success;

	public DeleteJobOpenapiVO(Long jobId, Boolean success) {
		this.jobId = jobId;
		Success = success;
	}

}

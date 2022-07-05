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
public class UpdateJobOpenapiVO {

	private final Long id;
	private final Boolean success;

	public UpdateJobOpenapiVO(Long id, Boolean success) {
		this.id = id;
		this.success = success;
	}

}

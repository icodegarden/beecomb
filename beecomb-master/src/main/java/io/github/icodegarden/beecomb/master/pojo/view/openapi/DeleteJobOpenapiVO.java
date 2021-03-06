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

	private final Long id;
	private final Boolean success;

	public DeleteJobOpenapiVO(Long id, Boolean success) {
		this.id = id;
		this.success = success;
	}

}

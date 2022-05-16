package io.github.icodegarden.beecomb.master.pojo.transfer.api;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UpdatePasswordApiDTO {

	@NotNull
	private Long id;
	@NotEmpty
	private String passwordOld;
	@NotEmpty
	private String passwordNew;
}

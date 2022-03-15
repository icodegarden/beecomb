package io.github.icodegarden.beecomb.master.pojo.transfer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UpdatePasswordNonOldDTO {

	@NotNull
	private Long id;
	@NotEmpty
	private String password;
}

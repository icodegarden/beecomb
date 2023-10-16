package io.github.icodegarden.beecomb.master.pojo.transfer.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UpdatePasswordNonOldApiDTO {

	@NotNull
	private Long id;
	@NotEmpty
	private String password;
}

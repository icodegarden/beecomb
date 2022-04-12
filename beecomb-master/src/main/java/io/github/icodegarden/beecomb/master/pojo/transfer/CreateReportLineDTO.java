package io.github.icodegarden.beecomb.master.pojo.transfer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class CreateReportLineDTO {

	@Size(max = 20)
	@NotEmpty
	private String type;
	@NotEmpty
	private String content;
}

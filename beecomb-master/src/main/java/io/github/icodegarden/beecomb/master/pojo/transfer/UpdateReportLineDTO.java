package io.github.icodegarden.beecomb.master.pojo.transfer;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class UpdateReportLineDTO {

	@NotNull
	private Long id;// bigint unsigned NOT NULL AUTO_INCREMENT,
	private String content;
}

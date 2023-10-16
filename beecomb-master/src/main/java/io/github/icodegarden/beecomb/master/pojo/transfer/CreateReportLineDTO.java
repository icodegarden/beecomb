package io.github.icodegarden.beecomb.master.pojo.transfer;

import io.github.icodegarden.beecomb.master.pojo.persistence.ReportLinePO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class CreateReportLineDTO {

	@NotNull
	private ReportLinePO.Type type;
	@NotEmpty
	private String content;
}

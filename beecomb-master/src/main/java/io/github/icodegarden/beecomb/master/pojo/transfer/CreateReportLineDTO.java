package io.github.icodegarden.beecomb.master.pojo.transfer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.github.icodegarden.beecomb.master.pojo.persistence.ReportLinePO;
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

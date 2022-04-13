package io.github.icodegarden.beecomb.common.backend.pojo.view;
import io.github.icodegarden.beecomb.common.enums.JobType;
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
public class JobExecuteRecordCountVO {

	private Long count;
	private JobType type;
	private String createdBy;
	private Boolean success;
}

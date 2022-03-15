package io.github.icodegarden.beecomb.master.pojo.data;

import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.master.pojo.persistence.JobRecoveryRecordPO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class JobRecoveryRecordDO {

	private JobRecoveryRecordPO jobRecoveryRecord;
	private JobMainPO jobMain;

}

package io.github.icodegarden.beecomb.master.service;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.backend.manager.JobExecuteRecordManager;
import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobExecuteRecordCountVO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainCountVO;
import io.github.icodegarden.beecomb.master.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.master.manager.ReportLineManager;
import io.github.icodegarden.beecomb.master.pojo.persistence.ReportLinePO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateReportLineDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdateReportLineDTO;
import io.github.icodegarden.commons.zookeeper.concurrent.lock.ZooKeeperLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class ReportService {

	@Autowired
	private JobMainManager jobMainManager;
	@Autowired
	private JobExecuteRecordManager jobExecuteRecordManager;
	@Autowired
	private ReportLineManager reportLineManager;

	private final ZooKeeperLock lock;

	public ReportService(CuratorFramework client, InstanceProperties instanceProperties) {
		lock = new ZooKeeperLock(client, instanceProperties.getZookeeper().getLockRoot(), "updateReport");
	}

	/**
	 * 更新报表结果
	 */
	public void update() {
		if (lock.acquire(1000)) {
			try {
				List<JobMainCountVO> countTotalGroupByTypeAndCreateBy = jobMainManager
						.countTotalGroupByTypeAndCreateBy();
				updateReportLine(ReportLinePO.Type.JobTotalDayN, countTotalGroupByTypeAndCreateBy);

				List<JobMainCountVO> countQueuedGroupByTypeAndCreateBy = jobMainManager
						.countQueuedGroupByTypeAndCreateBy();
				updateReportLine(ReportLinePO.Type.JobQueuedDayN, countQueuedGroupByTypeAndCreateBy);

				List<JobMainCountVO> countNoQueuedNoEndGroupByTypeAndCreateBy = jobMainManager
						.countNoQueuedNoEndGroupByTypeAndCreateBy();
				updateReportLine(ReportLinePO.Type.JobNoQueuedNoEndDayN, countNoQueuedNoEndGroupByTypeAndCreateBy);

				List<JobMainCountVO> countEndLastExecuteSuccessGroupByTypeAndCreateBy = jobMainManager
						.countEndLastExecuteSuccessGroupByTypeAndCreateBy();
				updateReportLine(ReportLinePO.Type.JobEndLastExecuteSuccessDayN,
						countEndLastExecuteSuccessGroupByTypeAndCreateBy);

				List<JobExecuteRecordCountVO> countTotalGroupByTypeAndCreateByAndSuccess = jobExecuteRecordManager
						.countTotalGroupByTypeAndCreateByAndSuccess();
				updateReportLine(ReportLinePO.Type.JobExecuteRecordTotalDayN,
						countTotalGroupByTypeAndCreateByAndSuccess);
			} finally {
				lock.release();
			}
		}
	}

	private void updateReportLine(ReportLinePO.Type type, Object dataOfContent) {
		ReportLinePO jobTotalDayN = reportLineManager.findOneByType(type, null);
		if (jobTotalDayN == null) {
			CreateReportLineDTO dto = new CreateReportLineDTO();
			dto.setContent(ReportLinePO.contentOfJsonArray(null, dataOfContent, 30));
			dto.setType(type);
			reportLineManager.create(dto);
		} else {
			UpdateReportLineDTO dto = new UpdateReportLineDTO();
			dto.setId(jobTotalDayN.getId());
			dto.setContent(ReportLinePO.contentOfJsonArray(jobTotalDayN.getContent(), dataOfContent, 30));
			reportLineManager.update(dto);
		}
	}

}

package io.github.icodegarden.beecomb.master.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.backend.manager.JobExecuteRecordManager;
import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobExecuteRecordCountVO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainCountVO;
import io.github.icodegarden.beecomb.master.manager.ReportLineManager;
import io.github.icodegarden.beecomb.master.pojo.persistence.ReportLinePO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateReportLineDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdateReportLineDTO;

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

	/**
	 * 更新报表结果
	 */
	public void update() {
//		List<JobMainCountVO> countTotalGroupByTypeAndCreateBy = jobMainManager.countTotalGroupByTypeAndCreateBy();
//		updateReportLine(ReportLinePO.Type.JobTotalDayN, countTotalGroupByTypeAndCreateBy);
		List<JobMainCountVO> countDayIncrGroupByTypeAndCreateBy = jobMainManager.countDayIncrGroupByTypeAndCreateBy();
		updateReportLine(ReportLinePO.Type.JobTotalDayN, countDayIncrGroupByTypeAndCreateBy, true);

		List<JobMainCountVO> countQueuedGroupByTypeAndCreateBy = jobMainManager.countQueuedGroupByTypeAndCreateBy();
		updateReportLine(ReportLinePO.Type.JobQueuedDayN, countQueuedGroupByTypeAndCreateBy);

		List<JobMainCountVO> countNoQueuedNoEndGroupByTypeAndCreateBy = jobMainManager
				.countNoQueuedNoEndGroupByTypeAndCreateBy();
		updateReportLine(ReportLinePO.Type.JobNoQueuedNoEndDayN, countNoQueuedNoEndGroupByTypeAndCreateBy);

//		List<JobMainCountVO> countEndLastExecuteSuccessGroupByTypeAndCreateBy = jobMainManager
//				.countEndLastExecuteSuccessGroupByTypeAndCreateBy();
//		updateReportLine(ReportLinePO.Type.JobEndLastExecuteSuccessDayN,
//				countEndLastExecuteSuccessGroupByTypeAndCreateBy);
		List<JobMainCountVO> countDayIncrEndLastExecuteSuccessGroupByTypeAndCreateBy = jobMainManager
				.countDayIncrEndLastExecuteSuccessGroupByTypeAndCreateBy();
		updateReportLine(ReportLinePO.Type.JobEndLastExecuteSuccessDayN,
				countDayIncrEndLastExecuteSuccessGroupByTypeAndCreateBy, true);

//		List<JobMainCountVO> countEndLastExecuteFailedGroupByTypeAndCreateBy = jobMainManager
//				.countEndLastExecuteFailedGroupByTypeAndCreateBy();
//		updateReportLine(ReportLinePO.Type.JobEndLastExecuteFailedDayN,
//				countEndLastExecuteFailedGroupByTypeAndCreateBy);
		List<JobMainCountVO> countDayIncrEndLastExecuteFailedGroupByTypeAndCreateBy = jobMainManager
				.countDayIncrEndLastExecuteFailedGroupByTypeAndCreateBy();
		updateReportLine(ReportLinePO.Type.JobEndLastExecuteFailedDayN,
				countDayIncrEndLastExecuteFailedGroupByTypeAndCreateBy, true);

//		List<JobExecuteRecordCountVO> countTotalGroupByTypeAndCreateByAndSuccess = jobExecuteRecordManager
//				.countTotalGroupByTypeAndCreateByAndSuccess();
//		updateReportLine(ReportLinePO.Type.JobExecuteRecordTotalDayN, countTotalGroupByTypeAndCreateByAndSuccess);
		List<JobExecuteRecordCountVO> countDayIncrGroupByTypeAndCreateByAndSuccess = jobExecuteRecordManager
				.countDayIncrGroupByTypeAndCreateByAndSuccess();
		updateReportLine(ReportLinePO.Type.JobExecuteRecordTotalDayN, countDayIncrGroupByTypeAndCreateByAndSuccess,
				true);
	}

	private void updateReportLine(ReportLinePO.Type type, List dataOfContent) {
		updateReportLine(type, dataOfContent, false);
	}

	private void updateReportLine(ReportLinePO.Type type, List dataOfContent, boolean dayIncr) {
		ReportLinePO jobTotalDayN = reportLineManager.findOneByType(type, null);
		if (jobTotalDayN == null) {
			CreateReportLineDTO dto = new CreateReportLineDTO();
			dto.setContent(ReportLinePO.contentOfJsonArray(dayIncr, null, dataOfContent, 30));
			dto.setType(type);
			reportLineManager.create(dto);
		} else {
			UpdateReportLineDTO dto = new UpdateReportLineDTO();
			dto.setId(jobTotalDayN.getId());
			dto.setContent(ReportLinePO.contentOfJsonArray(dayIncr, jobTotalDayN.getContent(), dataOfContent, 30));
			reportLineManager.update(dto);
		}
	}
}

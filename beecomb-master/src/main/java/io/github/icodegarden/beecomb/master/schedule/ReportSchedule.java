package io.github.icodegarden.beecomb.master.schedule;

import io.github.icodegarden.beecomb.master.service.ReportService;
import io.github.icodegarden.nutrient.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.nutrient.lang.schedule.LockSupportSchedule;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class ReportSchedule extends LockSupportSchedule {

	private final ReportService reportService;

	public ReportSchedule(DistributedLock lock, ReportService reportService) {
		super(lock);
		this.reportService = reportService;
	}

	@Override
	protected void doScheduleAfterLocked() throws Throwable {
		if (log.isInfoEnabled()) {
			log.info("schedule updateReport triggered");
		}
		reportService.update();
	}
}

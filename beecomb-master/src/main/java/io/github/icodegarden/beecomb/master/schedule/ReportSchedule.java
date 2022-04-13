package io.github.icodegarden.beecomb.master.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.github.icodegarden.beecomb.master.service.ReportService;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Component
public class ReportSchedule {

	@Autowired
	private ReportService reportService;

	/**
	 * 每天凌晨2点执行
	 */
	@Scheduled(cron = "0 0 2 * * *")
	void updateReport() {
		if (log.isInfoEnabled()) {
			log.info("schedule updateReport triggered");
		}
		reportService.update();
	}
}

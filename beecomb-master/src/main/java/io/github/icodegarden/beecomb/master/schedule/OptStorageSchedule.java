package io.github.icodegarden.beecomb.master.schedule;

import io.github.icodegarden.beecomb.master.manager.TableManager;
import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.schedule.LockSupportSchedule;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class OptStorageSchedule extends LockSupportSchedule {

	private final int deleteBeforeDays;
	private final TableManager tableManager;

	public OptStorageSchedule(DistributedLock lock, int deleteBeforeDays, TableManager tableManager) {
		super(lock);
		this.deleteBeforeDays = deleteBeforeDays;
		this.tableManager = tableManager;
	}

	@Override
	protected void doScheduleAfterLocked() throws Throwable {
		if (log.isInfoEnabled()) {
			log.info("schedule optStorageSpace triggered");
		}
		tableManager.optStorageSpace(deleteBeforeDays);
	}
}

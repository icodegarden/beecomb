package io.github.icodegarden.beecomb.master.manager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.backend.manager.TableDataCountManager;
import io.github.icodegarden.beecomb.common.backend.mapper.SqlMapper;
import io.github.icodegarden.beecomb.master.mapper.TableDataCountMapper;
import io.github.icodegarden.beecomb.master.pojo.persistence.TableDataCountPO;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.lang.util.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Service
public class TableDataCountManagerImpl implements TableDataCountManager {

	private Map<String, Long> cache;

	@Autowired
	private TableDataCountMapper tableDataCountMapper;
	@Autowired
	private TableService tableService;

	@Value("${server.table.countThreshold:500000}")
	private Long countThreshold;

	@PostConstruct
	private void init() {
		ScheduledThreadPoolExecutor scheduledThreadPool = ThreadPoolUtils
				.newSingleScheduledThreadPool("TableDataCount-Schedule");
		scheduledThreadPool.scheduleWithFixedDelay(() -> {
			try {
				doSchedule();
			} catch (Exception e) {
				log.error("WARN ex on refreshCache", e);
			}
		}, 5, 3600, TimeUnit.SECONDS);
	}

	/**
	 * 缓存中的表缺少，则补充<br>
	 * 缓存中的count已超过，不再处理；否则更新count值
	 */
	private void doSchedule() {
		List<String> tables = tableService.listTables();

		refreshCacheIfNull();

		for (String table : tables) {
			Long v = cache.get(table);
			if (v == null) {
				// 新增，可能并发
				try {
					create(table);
				} catch (DuplicateKeyException ignore) {
					log.info("duplicate table name:{} on table", table);
				}
				v = 0L;
			}

			if (v < countThreshold) {
				/**
				 * 由于table_data_count不分库，下面2条sql不能合并
				 */
				long count = tableService.countTable(table);
				tableDataCountMapper.updateCount(table, count);

				cache.put(table, count);
			}
		}
	}

	private void refreshCacheIfNull() {
		if (cache == null) {
			List<TableDataCountPO> list = list();
			cache = list.stream()
					.collect(Collectors.toMap(TableDataCountPO::getTableName, TableDataCountPO::getDataCount));
		}
	}

	@Override
	public boolean allowCount(String tableName) {
		refreshCacheIfNull();

		Long count = cache.get(tableName);
		if (count == null) {
			return true;
		}
		return count < countThreshold;
	}

	private void create(String tableName) {
		TableDataCountPO po = new TableDataCountPO();
		po.setTableName(tableName);
		po.setUpdatedAt(SystemUtils.now());
		tableDataCountMapper.add(po);
	}

	private List<TableDataCountPO> list() {
		List<TableDataCountPO> list = tableDataCountMapper.findAll();
		return list;
	}

	public interface TableService {
		List<String> listTables();

		long countTable(String tableName);
	}

	@Service
	public class TableServiceImpl implements TableService {
		private Set<String> whiteListTables = new HashSet<String>(Arrays.asList("job_main", "job_detail", "delay_job",
				"schedule_job", "job_execute_record", "job_recovery_record", "pending_recovery_job"));
//		private Set<String> blackListTables = new HashSet<String>(Arrays.asList("table_data_count", "id_sequence"));

		private List<String> tables;

		@Autowired
		private SqlMapper sqlMapper;

		@Override
		public List<String> listTables() {
			if (tables == null) {
				tables = sqlMapper.showTables().stream().filter(table -> {
//					return !blackListTables.contains(table);
					return whiteListTables.contains(table);
				}).collect(Collectors.toList());
			}
			return tables;
		}

		@Override
		public long countTable(String tableName) {
			long count = sqlMapper.countAll(tableName);
			return count;
		}

	}
}

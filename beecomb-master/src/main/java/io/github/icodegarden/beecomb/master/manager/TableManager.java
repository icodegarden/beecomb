package io.github.icodegarden.beecomb.master.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.icodegarden.commons.lang.repository.Database;
import io.github.icodegarden.commons.lang.repository.MysqlJdbcDatabase;
import io.github.icodegarden.commons.lang.repository.OptimizeTableResults;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.shardingsphere.util.DataSourceUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Service
public class TableManager {

	@Autowired
	private DataSource dataSource;

	/**
	 * 优化存储空间
	 * 
	 * @param deleteBeforeDays 删除N天之前的
	 */
	public void optStorageSpace(int deleteBeforeDays) {
		log.info("optStorageSpace start");

		Map<String, DataSource> dataSourceMap = DataSourceUtils.dataSourceMap((ShardingSphereDataSource) dataSource);

		for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
			String dsName = entry.getKey();
			DataSource ds = entry.getValue();
			log.info("optStorageSpace datasource name:{}", dsName);

			/**
			 * 删除过期数据
			 */
			String trigAtLt = SystemUtils.STANDARD_DATETIME_FORMATTER
					.format(SystemUtils.now().minusDays(deleteBeforeDays));
			try {
				int i = deleteJobExecuteRecord(ds, trigAtLt);
				log.info("optStorageSpace deleteJobExecuteRecord count:{}", i);
			} catch (Exception e) {
				log.error("ex on deleteJobExecuteRecord, contitional:{}", trigAtLt, e);
			}

			try {
				Thread.sleep(5000);// 停顿
			} catch (InterruptedException e1) {
			}

			/**
			 * 执行Optimize
			 */
			List<String> tables = listOptimizeTables(ds);
			for (String tableName : tables) {
				log.info("optStorageSpace optimizeTable start {}.{}", dsName, tableName);
				try {
					String desc = optimizeTable(ds, tableName);
					log.info("optStorageSpace optimizeTable end {}.{}, desc:{}", dsName, tableName, desc);
				} catch (Exception e) {
					log.error("ex on optimizeTable table:{}.{}", dsName, tableName, e);
				}

				try {
					Thread.sleep(3000);// 停顿
				} catch (InterruptedException e1) {
				}
			}
		}
		log.info("optStorageSpace end");
	}

	private int deleteJobExecuteRecord(DataSource dataSource, String trigAtLt/* yyyy-MM-dd HH:mm:ss */) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = "delete from job_execute_record where trig_at < '" + trigAtLt + "'";
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				return ptmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new IllegalStateException(String.format("delete from error, job_execute_record"), e);
		}

	}

	private List<String> listOptimizeTables(DataSource dataSource) {
		Database database = new MysqlJdbcDatabase(dataSource);
		return database.listTables();
	}

	private String optimizeTable(DataSource dataSource, String tableName) {
		Database database = new MysqlJdbcDatabase(dataSource);
		OptimizeTableResults<OptimizeTableResults.Result> results = database.optimizeTable(tableName);
		if (results.isErrorInMysql()) {
			throw new IllegalStateException(results.getDesc());
		}
		return results.getDesc();
	}
}

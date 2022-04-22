package io.github.icodegarden.beecomb.common.backend.shardingsphere;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import io.github.icodegarden.commons.shardingsphere.properties.Datasource;
import io.github.icodegarden.commons.shardingsphere.util.DataSourceUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class ApiShardingSphereBuilder {

	public static DataSource getDataSource(BeecombShardingsphereProperties properties) throws SQLException {
		return ShardingSphereDataSourceFactory.createDataSource(
				DataSourceUtils.createDataSourceMap(properties.getDatasources()),
				Collections.singleton(createShardingRuleConfiguration(properties.getDatasources())), new Properties());
	}

	private static ShardingRuleConfiguration createShardingRuleConfiguration(List<Datasource> datasources) {
		ShardingRuleConfiguration result = new ShardingRuleConfiguration();
		result.getTables().add(getJobMainRuleConfiguration(datasources));
		result.getTables().add(getJobDetailTableRuleConfiguration(datasources));
		result.getTables().add(getDelayJobTableRuleConfiguration(datasources));
		result.getTables().add(getScheduleJobTableRuleConfiguration(datasources));
		result.getTables().add(getJobExecuteRecordTableRuleConfiguration(datasources));
		result.getTables().add(getJobRecoveryRecordTableRuleConfiguration(datasources));

		result.getBindingTableGroups()
				.add("job_main,job_detail,delay_job,schedule_job,job_execute_record,job_recovery_record");
//		result.getBroadcastTables().add("t_address");
//		result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "inline"));

		Properties props = new Properties();
		props = new Properties();
		props.setProperty("strategy", "standard");
		props.setProperty("algorithmClassName",
				"io.github.icodegarden.commons.shardingsphere.algorithm.RangeModShardingAlgorithm");
		props.setProperty("name", "jobidrangemod");
		result.getShardingAlgorithms().put("jobidrangemod",
				new ShardingSphereAlgorithmConfiguration("CLASS_BASED", props));

		Properties mysqljobmainProps = new Properties();
		mysqljobmainProps.setProperty("moduleName", "job_main");
		result.getKeyGenerators().put("mysqljobmain",
				new ShardingSphereAlgorithmConfiguration("MYSQL", mysqljobmainProps));

		Properties mysqljobexecuterrecordProps = new Properties();
		mysqljobexecuterrecordProps.setProperty("moduleName", "job_execute_record");
		result.getKeyGenerators().put("mysqljobexecuterrecord",
				new ShardingSphereAlgorithmConfiguration("MYSQL", mysqljobexecuterrecordProps));

		return result;
	}

	private static ShardingTableRuleConfiguration getJobMainRuleConfiguration(List<Datasource> datasources) {
		String actualDataNodes = datasources.stream().map(ds -> {
			return ds.getName() + ".job_main";
		}).collect(Collectors.joining(","));

		log.info("actualDataNodes of job_main:{}", actualDataNodes);

		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("job_main", actualDataNodes);

		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration("id",
				"jobidrangemod");
		result.setDatabaseShardingStrategy(databaseShardingStrategy);

		result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "mysqljobmain"));
		return result;
	}

	private static ShardingTableRuleConfiguration getJobDetailTableRuleConfiguration(List<Datasource> datasources) {
		String actualDataNodes = datasources.stream().map(ds -> {
			return ds.getName() + ".job_detail";
		}).collect(Collectors.joining(","));

		log.info("actualDataNodes of job_detail:{}", actualDataNodes);

		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("job_detail", actualDataNodes);

		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration(
				"job_id", "jobidrangemod");
		result.setDatabaseShardingStrategy(databaseShardingStrategy);

		return result;
	}

	private static ShardingTableRuleConfiguration getDelayJobTableRuleConfiguration(List<Datasource> datasources) {
		String actualDataNodes = datasources.stream().map(ds -> {
			return ds.getName() + ".delay_job";
		}).collect(Collectors.joining(","));

		log.info("actualDataNodes of delay_job:{}", actualDataNodes);

		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("delay_job", actualDataNodes);

		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration(
				"job_id", "jobidrangemod");
		result.setDatabaseShardingStrategy(databaseShardingStrategy);

		return result;
	}

	private static ShardingTableRuleConfiguration getScheduleJobTableRuleConfiguration(List<Datasource> datasources) {
		String actualDataNodes = datasources.stream().map(ds -> {
			return ds.getName() + ".schedule_job";
		}).collect(Collectors.joining(","));

		log.info("actualDataNodes of schedule_job:{}", actualDataNodes);

		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("schedule_job", actualDataNodes);

		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration(
				"job_id", "jobidrangemod");
		result.setDatabaseShardingStrategy(databaseShardingStrategy);

		return result;
	}

	private static ShardingTableRuleConfiguration getJobExecuteRecordTableRuleConfiguration(
			List<Datasource> datasources) {
		String actualDataNodes = datasources.stream().map(ds -> {
			return ds.getName() + ".job_execute_record";
		}).collect(Collectors.joining(","));

		log.info("actualDataNodes of job_execute_record:{}", actualDataNodes);

		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("job_execute_record",
				actualDataNodes);

		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration(
				"job_id", "jobidrangemod");
		result.setDatabaseShardingStrategy(databaseShardingStrategy);

		return result;
	}

	private static ShardingTableRuleConfiguration getJobRecoveryRecordTableRuleConfiguration(
			List<Datasource> datasources) {
		String actualDataNodes = datasources.stream().map(ds -> {
			return ds.getName() + ".job_recovery_record";
		}).collect(Collectors.joining(","));

		log.info("actualDataNodes of job_recovery_record:{}", actualDataNodes);

		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("job_recovery_record",
				actualDataNodes);

		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration(
				"job_id", "jobidrangemod");
		result.setDatabaseShardingStrategy(databaseShardingStrategy);

		return result;
	}
}

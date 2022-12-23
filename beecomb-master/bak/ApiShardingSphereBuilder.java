package io.github.icodegarden.beecomb.master;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
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

import io.github.icodegarden.commons.shardingsphere.algorithm.MysqlKeyGenerateAlgorithm;
import io.github.icodegarden.commons.shardingsphere.algorithm.RangeModShardingAlgorithm;
import io.github.icodegarden.commons.shardingsphere.builder.DataSourceConfig;
import io.github.icodegarden.commons.shardingsphere.builder.RangeModShardingAlgorithmConfig;
import io.github.icodegarden.commons.shardingsphere.util.DataSourceUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class ApiShardingSphereBuilder {

	public static DataSource getDataSource(BeecombShardingsphereProperties beecombShardingsphereProperties) throws SQLException {
		RangeModShardingAlgorithmConfig jobidrangemod = beecombShardingsphereProperties.getJobidrangemod();
		jobidrangemod.validate();
		RangeModShardingAlgorithm.registerRangeModShardingAlgorithmConfig("jobidrangemod", jobidrangemod);
		
		LinkedHashMap<String, DataSource> dataSourceMap = DataSourceUtils.createDataSourceMap(beecombShardingsphereProperties.getDatasources());
		DataSource firstDataSource = DataSourceUtils.firstDataSource(dataSourceMap);
		MysqlKeyGenerateAlgorithm.registerDataSource(firstDataSource);
		
		return ShardingSphereDataSourceFactory.createDataSource(
				DataSourceUtils.createDataSourceMap(beecombShardingsphereProperties.getDatasources()),
				Collections.singleton(createShardingRuleConfiguration(beecombShardingsphereProperties.getDatasources())), new Properties());
	}

	private static ShardingRuleConfiguration createShardingRuleConfiguration(List<DataSourceConfig> dataSourceProperties) {
		ShardingRuleConfiguration result = new ShardingRuleConfiguration();
		result.getTables().add(getJobMainRuleConfiguration(dataSourceProperties));
		result.getTables().add(getJobDetailTableRuleConfiguration(dataSourceProperties));
		result.getTables().add(getDelayJobTableRuleConfiguration(dataSourceProperties));
		result.getTables().add(getScheduleJobTableRuleConfiguration(dataSourceProperties));
		result.getTables().add(getPendingRecoveryJobTableRuleConfiguration(dataSourceProperties));
		result.getTables().add(getJobExecuteRecordTableRuleConfiguration(dataSourceProperties));
		result.getTables().add(getJobRecoveryRecordTableRuleConfiguration(dataSourceProperties));

		/**
		 * 绑定表
		 */
		result.getBindingTableGroups().add(
				"job_main,job_detail,delay_job,schedule_job,pending_recovery_job,job_execute_record,job_recovery_record");
//		result.getBroadcastTables().add("t_address");
//		result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "inline"));

		/**
		 * 分片算法
		 */
		Properties props = new Properties();
		props.setProperty("strategy", "standard");
		props.setProperty("algorithmClassName",
				"io.github.icodegarden.commons.shardingsphere.algorithm.RangeModShardingAlgorithm");
		props.setProperty(RangeModShardingAlgorithm.ALGORITHM_NAME_KEY, "jobidrangemod");
		result.getShardingAlgorithms().put("jobidrangemod",
				new ShardingSphereAlgorithmConfiguration("CLASS_BASED", props));

		/**
		 * 全局id生成
		 */
		Properties mysqljobmainProps = new Properties();
		mysqljobmainProps.setProperty(MysqlKeyGenerateAlgorithm.MODULE_NAME_KEY, "job_main");
		result.getKeyGenerators().put("mysqljobmain",
				new ShardingSphereAlgorithmConfiguration(MysqlKeyGenerateAlgorithm.TYPE, mysqljobmainProps));
		/**
		 * 全局id生成
		 */
		Properties mysqljobexecuterrecordProps = new Properties();
		mysqljobexecuterrecordProps.setProperty(MysqlKeyGenerateAlgorithm.MODULE_NAME_KEY, "job_execute_record");
		result.getKeyGenerators().put("mysqljobexecuterrecord",
				new ShardingSphereAlgorithmConfiguration(MysqlKeyGenerateAlgorithm.TYPE, mysqljobexecuterrecordProps));

		return result;
	}

	/**
	 * job_main配置
	 */
	private static ShardingTableRuleConfiguration getJobMainRuleConfiguration(List<DataSourceConfig> dataSourceProperties) {
		String actualDataNodes = dataSourceProperties.stream().map(ds -> {
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

	/**
	 * job_detail配置
	 */
	private static ShardingTableRuleConfiguration getJobDetailTableRuleConfiguration(List<DataSourceConfig> dataSourceProperties) {
		String actualDataNodes = dataSourceProperties.stream().map(ds -> {
			return ds.getName() + ".job_detail";
		}).collect(Collectors.joining(","));

		log.info("actualDataNodes of job_detail:{}", actualDataNodes);

		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("job_detail", actualDataNodes);

		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration(
				"job_id", "jobidrangemod");
		result.setDatabaseShardingStrategy(databaseShardingStrategy);

		return result;
	}

	/**
	 * delay_job配置
	 */
	private static ShardingTableRuleConfiguration getDelayJobTableRuleConfiguration(List<DataSourceConfig> dataSourceProperties) {
		String actualDataNodes = dataSourceProperties.stream().map(ds -> {
			return ds.getName() + ".delay_job";
		}).collect(Collectors.joining(","));

		log.info("actualDataNodes of delay_job:{}", actualDataNodes);

		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("delay_job", actualDataNodes);

		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration(
				"job_id", "jobidrangemod");
		result.setDatabaseShardingStrategy(databaseShardingStrategy);

		return result;
	}

	/**
	 * schedule_job配置
	 */
	private static ShardingTableRuleConfiguration getScheduleJobTableRuleConfiguration(List<DataSourceConfig> dataSourceProperties) {
		String actualDataNodes = dataSourceProperties.stream().map(ds -> {
			return ds.getName() + ".schedule_job";
		}).collect(Collectors.joining(","));

		log.info("actualDataNodes of schedule_job:{}", actualDataNodes);

		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("schedule_job", actualDataNodes);

		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration(
				"job_id", "jobidrangemod");
		result.setDatabaseShardingStrategy(databaseShardingStrategy);

		return result;
	}

	/**
	 * 待恢复任务配置
	 */
	private static ShardingTableRuleConfiguration getPendingRecoveryJobTableRuleConfiguration(
			List<DataSourceConfig> dataSourceProperties) {
		String actualDataNodes = dataSourceProperties.stream().map(ds -> {
			return ds.getName() + ".pending_recovery_job";
		}).collect(Collectors.joining(","));

		log.info("actualDataNodes of pending_recovery_job:{}", actualDataNodes);

		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("pending_recovery_job",
				actualDataNodes);

		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration(
				"job_id", "jobidrangemod");
		result.setDatabaseShardingStrategy(databaseShardingStrategy);

		return result;
	}

	/**
	 * 任务执行记录配置
	 */
	private static ShardingTableRuleConfiguration getJobExecuteRecordTableRuleConfiguration(
			List<DataSourceConfig> dataSourceProperties) {
		String actualDataNodes = dataSourceProperties.stream().map(ds -> {
			return ds.getName() + ".job_execute_record";
		}).collect(Collectors.joining(","));

		log.info("actualDataNodes of job_execute_record:{}", actualDataNodes);

		ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("job_execute_record",
				actualDataNodes);

		StandardShardingStrategyConfiguration databaseShardingStrategy = new StandardShardingStrategyConfiguration(
				"job_id", "jobidrangemod");
		result.setDatabaseShardingStrategy(databaseShardingStrategy);

		result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "mysqljobexecuterrecord"));
		return result;
	}

	/**
	 * 任务恢复记录配置
	 */
	private static ShardingTableRuleConfiguration getJobRecoveryRecordTableRuleConfiguration(
			List<DataSourceConfig> dataSourceProperties) {
		String actualDataNodes = dataSourceProperties.stream().map(ds -> {
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

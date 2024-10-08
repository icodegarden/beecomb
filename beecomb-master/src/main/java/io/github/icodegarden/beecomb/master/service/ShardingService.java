package io.github.icodegarden.beecomb.master.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.backend.pojo.view.JobDetailVO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.nursery.springboot.shardingsphere.properties.NurseryShardingSphereProperties;
import io.github.icodegarden.nutrient.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.nutrient.lang.concurrent.lock.MysqlJdbcLock;
import io.github.icodegarden.nutrient.lang.spec.response.ClientBizErrorCodeException;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;
import io.github.icodegarden.nutrient.mybatis.concurrent.lock.MysqlMybatisLockMapper;
import io.github.icodegarden.nutrient.shardingsphere.builder.RangeModShardingAlgorithmConfig;
import io.github.icodegarden.nutrient.shardingsphere.builder.RangeModShardingAlgorithmConfig.Group;
import io.github.icodegarden.nutrient.shardingsphere.util.DataSourceUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@Service
public class ShardingService {

	@Autowired
	private DataSource dataSource;
	@Autowired
	private NurseryShardingSphereProperties shardingSphereProperties;
	@Autowired
	private MysqlMybatisLockMapper mapper;

	private Map<String, DataSource> dataSourceMap;

	@PostConstruct
	private void init() {
		dataSourceMap = DataSourceUtils.dataSourceMap((ShardingSphereDataSource) dataSource);
	}

	public List<Group> listGroups(String groupLike) {
		List<RangeModShardingAlgorithmConfig> list = shardingSphereProperties.getRangeModShardingAlgorithms();
		List<Group> groups = list.get(0).getGroups();
		if (groupLike != null) {
			groups = groups.stream().filter(g -> g.getName().contains(groupLike)).collect(Collectors.toList());
		}
		return groups;
	}

	public Group getGroup(String name) {
		List<RangeModShardingAlgorithmConfig> list = shardingSphereProperties.getRangeModShardingAlgorithms();

		List<Group> groups = list.get(0).getGroups();

		Group findGroup = groups.stream().filter(g -> g.getName().equals(name)).findFirst().get();
		return findGroup;
	}

	public JobMainVO getLastJob(String group) {
		Group findGroup = getGroup(group);
		Map<String, List<Integer>> mlb = findGroup.getMlb();

		JobMainVO vo = null;

		for (String dsName : mlb.keySet()) {
			DataSource ds = dataSourceMap.get(dsName);

			try (Connection connection = ds.getConnection();) {
				try (PreparedStatement ptmt = connection.prepareStatement(LAST_JOB_SQL);) {
					try (ResultSet rs = ptmt.executeQuery();) {
						while (rs.next()) {
							long id = rs.getLong("id");

							if (vo != null && vo.getId() > id) {
								continue;
							}

							vo = new JobMainVO();
							vo.setId(rs.getLong("id"));
							vo.setUuid(rs.getString("uuid"));
							vo.setName(rs.getString("name"));
							vo.setType(JobType.valueOf(rs.getString("type")));
							vo.setExecutorName(rs.getString("executor_name"));
							vo.setJobHandlerName(rs.getString("job_handler_name"));
							vo.setPriority(rs.getInt("priority"));
							vo.setWeight(rs.getInt("weight"));
							vo.setParallel(rs.getBoolean("is_parallel"));
							vo.setMaxParallelShards(rs.getInt("max_parallel_shards"));
							vo.setQueued(rs.getBoolean("is_queued"));

							String last_trig_at = rs.getString("last_trig_at");
							if (last_trig_at != null) {
								vo.setLastTrigAt(
										LocalDateTime.parse(last_trig_at, SystemUtils.STANDARD_DATETIME_FORMATTER));
							}

							vo.setExecuteTimeout(rs.getInt("execute_timeout"));

							String next_trig_at = rs.getString("next_trig_at");
							if (next_trig_at != null) {
								vo.setNextTrigAt(
										LocalDateTime.parse(next_trig_at, SystemUtils.STANDARD_DATETIME_FORMATTER));
							}

							vo.setEnd(rs.getBoolean("is_end"));
							vo.setLabel(rs.getString("label"));
							vo.setLastExecuteSuccess(rs.getBoolean("is_last_execute_success"));

							String queued_at = rs.getString("queued_at");
							if (queued_at != null) {
								vo.setQueuedAt(LocalDateTime.parse(queued_at, SystemUtils.STANDARD_DATETIME_FORMATTER));
							}

							vo.setQueuedAtInstance(rs.getString("queued_at_instance"));
							vo.setLastExecuteExecutor(rs.getString("last_execute_executor"));
							vo.setCreatedBy(rs.getString("created_by"));

							String created_at = rs.getString("created_at");
							vo.setCreatedAt(LocalDateTime.parse(created_at, SystemUtils.STANDARD_DATETIME_FORMATTER));

							JobDetailVO detailVO = new JobDetailVO();
							detailVO.setParams(rs.getString("jd_params"));
							detailVO.setDesc(rs.getString("jd_desc"));
							detailVO.setLastTrigResult(rs.getString("jd_last_trig_result"));
							detailVO.setLastExecuteReturns(rs.getString("jd_last_execute_returns"));
							vo.setJobDetail(detailVO);

						}
					}
				}
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}

		return vo;
	}

	public Page<JobMainVO> pageNotEndJobs(String group) {
		Page<JobMainVO> p = new Page<JobMainVO>(1, 10);

		Group findGroup = getGroup(group);
		Map<String, List<Integer>> mlb = findGroup.getMlb();

		for (String dsName : mlb.keySet()) {
			DataSource ds = dataSourceMap.get(dsName);

			try (Connection connection = ds.getConnection();) {
				try (PreparedStatement ptmt = connection.prepareStatement(NOT_END_JOB_SQL);) {
					try (ResultSet rs = ptmt.executeQuery();) {
						while (rs.next()) {
							if (p.size() == 10) {
								break;
							}
							JobMainVO vo = new JobMainVO();
							vo.setId(rs.getLong("id"));
							vo.setUuid(rs.getString("uuid"));
							vo.setName(rs.getString("name"));
							vo.setType(JobType.valueOf(rs.getString("type")));
							vo.setExecutorName(rs.getString("executor_name"));
							vo.setJobHandlerName(rs.getString("job_handler_name"));
							vo.setPriority(rs.getInt("priority"));
							vo.setWeight(rs.getInt("weight"));
							vo.setParallel(rs.getBoolean("is_parallel"));
							vo.setMaxParallelShards(rs.getInt("max_parallel_shards"));
							vo.setQueued(rs.getBoolean("is_queued"));

							String last_trig_at = rs.getString("last_trig_at");
							if (last_trig_at != null) {
								vo.setLastTrigAt(
										LocalDateTime.parse(last_trig_at, SystemUtils.STANDARD_DATETIME_FORMATTER));
							}

							vo.setExecuteTimeout(rs.getInt("execute_timeout"));

							String next_trig_at = rs.getString("next_trig_at");
							if (next_trig_at != null) {
								vo.setNextTrigAt(
										LocalDateTime.parse(next_trig_at, SystemUtils.STANDARD_DATETIME_FORMATTER));
							}

							vo.setEnd(rs.getBoolean("is_end"));
							vo.setLabel(rs.getString("label"));
							vo.setLastExecuteSuccess(rs.getBoolean("is_last_execute_success"));

							String queued_at = rs.getString("queued_at");
							if (queued_at != null) {
								vo.setQueuedAt(LocalDateTime.parse(queued_at, SystemUtils.STANDARD_DATETIME_FORMATTER));
							}

							vo.setQueuedAtInstance(rs.getString("queued_at_instance"));
							vo.setLastExecuteExecutor(rs.getString("last_execute_executor"));
							vo.setCreatedBy(rs.getString("created_by"));

							String created_at = rs.getString("created_at");
							vo.setCreatedAt(LocalDateTime.parse(created_at, SystemUtils.STANDARD_DATETIME_FORMATTER));

							p.add(vo);
						}
					}
				}
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}

		p.setTotal(p.size());

		return p;
	}

	public void deleteEndJobs(String group) {
		DataSource lockDs = DataSourceUtils.firstDataSource((ShardingSphereDataSource) dataSource);
		DistributedLock lock = new MysqlJdbcLock(lockDs, "deleteEndJobs", 7200L);
		if (lock.acquire(100)) {
			try {
				Group findGroup = getGroup(group);
				Map<String, List<Integer>> mlb = findGroup.getMlb();

				log.info("start deleteEndJobs from group[{}].", group);

				AtomicReference<Long> count = new AtomicReference<Long>(0L);

				for (String dsName : mlb.keySet()) {
					DataSource ds = dataSourceMap.get(dsName);

					deleteFromDs(ds, count);
				}

				log.info("end deleteEndJobs from group[{}], total[{}].", group, count.get());
			} finally {
				lock.release();
			}
		} else {
			throw new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN, "有删除任务还未结束，请耐心等待。");
		}
	}

	private void deleteFromDs(DataSource ds, AtomicReference<Long> count) {
		try (Connection connection = ds.getConnection();) {
			for (;;) {
				try (PreparedStatement ptmt = connection.prepareStatement(END_JOB_SQL);) {
					try (ResultSet rs = ptmt.executeQuery();) {

						List<Long> ids = new ArrayList<Long>(1000);
						while (rs.next()) {
							long id = rs.getLong("id");
							ids.add(id);
						}

						if (ids.isEmpty()) {
							break;
						}

						String idsStr = ids.stream().map(l -> l.toString()).collect(Collectors.joining(","));

						StringBuilder deleteMain = new StringBuilder(ids.size() * 8);
						deleteMain.append("delete from job_main where id in (");
						deleteMain.append(idsStr);
						deleteMain.append(")");
						execSql(connection, deleteMain.toString());

						StringBuilder deleteDetail = new StringBuilder(ids.size() * 8);
						deleteDetail.append("delete from job_detail where job_id in (");
						deleteDetail.append(idsStr);
						deleteDetail.append(")");
						execSql(connection, deleteDetail.toString());

						StringBuilder deleteDelay = new StringBuilder(ids.size() * 8);
						deleteDelay.append("delete from delay_job where job_id in (");
						deleteDelay.append(idsStr);
						deleteDelay.append(")");
						execSql(connection, deleteDelay.toString());

						StringBuilder deleteSchedule = new StringBuilder(ids.size() * 8);
						deleteSchedule.append("delete from schedule_job where job_id in (");
						deleteSchedule.append(idsStr);
						deleteSchedule.append(")");
						execSql(connection, deleteSchedule.toString());

						count.set(count.get() + ids.size());
					}
				}
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	private void execSql(Connection connection, String sql) {
		try {
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				ptmt.execute();
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	private static final String LAST_JOB_SQL = """
			select
						a.`id` as id,
						a.`uuid` as uuid,
						a.`name` as name,
						a.`type` as type,
						a.`executor_name` as executor_name,
						a.`job_handler_name` as job_handler_name,
						a.`priority` as priority,
						a.`weight` as weight,
						a.`is_parallel` as is_parallel,
						a.`max_parallel_shards` as max_parallel_shards,
						a.`is_queued` as is_queued,
						a.`last_trig_at` as last_trig_at,
						a.`execute_timeout` as execute_timeout,
						a.`next_trig_at` as next_trig_at,
						a.`is_end` as is_end,
						a.`label` as label,
						a.`is_last_execute_success` as is_last_execute_success,
						a.`queued_at` as queued_at,
						a.`queued_at_instance` as queued_at_instance,
						a.`last_execute_executor` as last_execute_executor,
						a.`created_by` as created_by,
						a.`created_at` as created_at,
						a.`updated_at` as updated_at,
						b.`job_id` as jd_job_id,
						b.`params` as jd_params,
						b.`desc` as jd_desc,
						b.`last_trig_result` as jd_last_trig_result,
						b.`last_execute_returns` as jd_last_execute_returns
			from job_main a left join `job_detail` b on a.id = b.job_id
			order by a.id desc
			limit 1
							""";

	private static final String NOT_END_JOB_SQL = """
			select
						a.`id` as id,
						a.`uuid` as uuid,
						a.`name` as name,
						a.`type` as type,
						a.`executor_name` as executor_name,
						a.`job_handler_name` as job_handler_name,
						a.`priority` as priority,
						a.`weight` as weight,
						a.`is_parallel` as is_parallel,
						a.`max_parallel_shards` as max_parallel_shards,
						a.`is_queued` as is_queued,
						a.`last_trig_at` as last_trig_at,
						a.`execute_timeout` as execute_timeout,
						a.`next_trig_at` as next_trig_at,
						a.`is_end` as is_end,
						a.`label` as label,
						a.`is_last_execute_success` as is_last_execute_success,
						a.`queued_at` as queued_at,
						a.`queued_at_instance` as queued_at_instance,
						a.`last_execute_executor` as last_execute_executor,
						a.`created_by` as created_by,
						a.`created_at` as created_at,
						a.`updated_at` as updated_at
			from job_main a
			where a.is_end = 0
			order by a.id desc
			limit 10
							""";

	private static final String END_JOB_SQL = """
			select
						a.`id`
			from job_main a
			where a.is_end = 1
			limit 1000
							""";
}

package io.github.icodegarden.beecomb.master.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.db.pojo.data.JobDO;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobQuery;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobService {

	@Autowired
	private JobMainMapper jobMainMapper;

	public boolean hasNoQueuedActually(LocalDateTime nextTrigAtLt) {
		JobQuery query = JobQuery.builder().nextTrigAtLt(nextTrigAtLt).limit("limit 1").build();
		List<JobDO> dos = jobMainMapper.findAll(query);
		return dos.size() >= 1;
	}

	/**
	 * 更新nextTrigAt超过给定的时间->状态未队列
	 * 
	 * @param nextTrigAtLt
	 * @return
	 */
	public int recoveryThatNoQueuedActually(LocalDateTime nextTrigAtLt) {
		return jobMainMapper.updateToNoQueued(nextTrigAtLt);
	}

	/**
	 * 获取 未完成且未队列 的任务，这些任务应该被恢复<br>
	 * 未end、未queued、按priority优先级
	 * 
	 * @param skip
	 * @param size
	 * @return
	 */
	public List<ExecutableJobBO> listJobsShouldRecovery(int skip, int size) {
		JobQuery query = JobQuery.builder().end(false).queued(false).with(JobQuery.With.WITH_EXECUTABLE)
				.sort("order by a.priority desc").limit("limit " + skip + "," + size).build();

		List<JobDO> dos = jobMainMapper.findAll(query);
		if (dos.isEmpty()) {
			return Collections.emptyList();
		}
		return dos.stream().map(JobDO::toExecutableJobBO).collect(Collectors.toList());
	}

}

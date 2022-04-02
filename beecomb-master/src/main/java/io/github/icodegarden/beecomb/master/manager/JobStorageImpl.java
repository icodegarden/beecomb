package io.github.icodegarden.beecomb.master.manager;

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
public class JobStorageImpl implements JobStorage {

	@Autowired
	private JobMainMapper jobMainMapper;

	@Override
	public boolean hasNoQueuedActually(LocalDateTime nextTrigAtLt) {
		JobQuery query = JobQuery.builder().nextTrigAtLt(nextTrigAtLt).limit("limit 1").build();
		List<JobDO> dos = jobMainMapper.findAll(query);
		return dos.size() >= 1;
	}

	@Override
	public int recoveryThatNoQueuedActually(LocalDateTime nextTrigAtLt) {
		return jobMainMapper.updateToNoQueued(nextTrigAtLt);
	}

	/**
	 * 未end、未queued、按priority优先级
	 */
	@Override
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

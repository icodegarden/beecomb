package io.github.icodegarden.beecomb.master.service;

import java.time.LocalDateTime;
import java.util.List;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobStorage {

	ExecutableJobBO create(CreateJobDTO dto) throws IllegalArgumentException;

	ExecutableJobBO findOneExecutableJob(Long id);
	
	boolean hasNoQueuedActually(LocalDateTime nextTrigAtLt);
	
	/**
	 * 更新nextTrigAt超过给定的时间->状态未队列
	 * @param nextTrigAtLt
	 * @return
	 */
	int recoveryThatNoQueuedActually(LocalDateTime nextTrigAtLt);
	/**
	 * 获取 未完成且未队列 的任务，这些任务应该被恢复
	 * @param skip
	 * @param size
	 * @return
	 */
	List<ExecutableJobBO> listJobsShouldRecovery(int skip, int size);
}

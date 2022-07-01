package io.github.icodegarden.beecomb.client;

import java.io.Closeable;

import io.github.icodegarden.beecomb.client.pojo.query.JobQuery;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.client.pojo.transfer.UpdateJobDTO;
import io.github.icodegarden.beecomb.client.pojo.view.CreateJobVO;
import io.github.icodegarden.beecomb.client.pojo.view.JobVO;
import io.github.icodegarden.beecomb.client.pojo.view.PageVO;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface BeeCombClient extends Closeable {

	CreateJobVO createJob(CreateJobDTO job) throws ExchangeException;

	CreateJobVO createJobAsync(CreateJobDTO job) throws ExchangeException;

	PageVO<JobVO> pageJobs(JobQuery query) throws ExchangeException;

	JobVO getJob(Long jobId) throws ExchangeException;

	JobVO getJobByUUID(String uuid) throws ExchangeException;
	
	void updateJob(UpdateJobDTO update) throws ExchangeException;
	
	/**
	 * delay任务在已经完成 或 已经取消时会失败<br>
	 * 
	 * @throws ExchangeException
	 */
	void deleteJob(Long jobId) throws ExchangeException;
}

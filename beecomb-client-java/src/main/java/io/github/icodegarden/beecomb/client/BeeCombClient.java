package io.github.icodegarden.beecomb.client;

import java.io.Closeable;

import io.github.icodegarden.beecomb.client.pojo.query.JobQuery;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.client.pojo.transfer.UpdateJobDTO;
import io.github.icodegarden.beecomb.client.pojo.view.CreateJobVO;
import io.github.icodegarden.beecomb.client.pojo.view.DeleteJobVO;
import io.github.icodegarden.beecomb.client.pojo.view.JobVO;
import io.github.icodegarden.beecomb.client.pojo.view.PageVO;
import io.github.icodegarden.beecomb.client.pojo.view.UpdateJobVO;
import io.github.icodegarden.nutrient.exchange.exception.ExchangeException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface BeeCombClient extends Closeable {

	/**
	 * 创建任务
	 * @param job
	 * @return
	 * @throws ExchangeException
	 */
	CreateJobVO createJob(CreateJobDTO job) throws ExchangeException;
	/**
	 * 创建任务，异步分配
	 * @param job
	 * @return
	 * @throws ExchangeException
	 */
	CreateJobVO createJobAsync(CreateJobDTO job) throws ExchangeException;
	
	PageVO<JobVO> pageJobs(JobQuery query) throws ExchangeException;
	
	JobVO getJob(Long jobId) throws ExchangeException;
	/**
	 * 使用uuid查询任务，uuid由用户自己控制唯一性，如果不是唯一的也只返回1条
	 * @param uuid
	 * @return
	 * @throws ExchangeException
	 */
	JobVO getJobByUUID(String uuid) throws ExchangeException;
	/**
	 * 如果更新任务的执行时间，则从更新时间开始重新计时
	 * @param update
	 * @return
	 * @throws ExchangeException
	 */
	UpdateJobVO updateJob(UpdateJobDTO update) throws ExchangeException;
	
	/**
	 * 任务在已经完成 或 已经取消时会失败<br>
	 * 
	 * @throws ExchangeException
	 */
	DeleteJobVO deleteJob(Long jobId) throws ExchangeException;
}

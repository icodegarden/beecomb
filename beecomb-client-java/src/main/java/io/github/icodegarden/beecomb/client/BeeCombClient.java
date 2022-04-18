package io.github.icodegarden.beecomb.client;

import java.io.Closeable;

import io.github.icodegarden.beecomb.client.pojo.request.JobQuery;
import io.github.icodegarden.beecomb.client.pojo.response.CreateJobResponse;
import io.github.icodegarden.beecomb.client.pojo.response.GetJobResponse;
import io.github.icodegarden.beecomb.client.pojo.response.PageResponse;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface BeeCombClient extends Closeable {

	CreateJobResponse createJob(CreateJobDTO job) throws ExchangeException;
	
	CreateJobResponse createJobAsync(CreateJobDTO job) throws ExchangeException;
	
	PageResponse<GetJobResponse> pageJobs(JobQuery query) throws ExchangeException;
	
	GetJobResponse getJob(Long jobId)throws ExchangeException;
	
	GetJobResponse getJobByUUID(String uuid)throws ExchangeException;

}

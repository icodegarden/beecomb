package io.github.icodegarden.beecomb.client;

import java.io.Closeable;

import io.github.icodegarden.beecomb.client.pojo.request.JobQuery;
import io.github.icodegarden.beecomb.client.pojo.response.CreateJobResponse;
import io.github.icodegarden.beecomb.client.pojo.response.FindJobResponse;
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
	
	PageResponse<FindJobResponse> pageJobs(JobQuery query) throws ExchangeException;
	
	FindJobResponse findJob(Long jobId)throws ExchangeException;
	
	FindJobResponse findJobByUUID(String uuid)throws ExchangeException;

}

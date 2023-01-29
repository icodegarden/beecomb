package io.github.icodegarden.beecomb.master.service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;
import io.github.icodegarden.commons.lang.concurrent.NamedThreadFactory;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
import io.github.icodegarden.commons.lang.result.Result2;
import io.github.icodegarden.commons.lang.result.Results;
import io.github.icodegarden.commons.lang.spec.response.ClientLimitedErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ErrorCodeException;
import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class JobReceiver {

	/**
	 * 这个线程池的处理是轻量的，以网络io为主，数量与本服务的web线程池大小相当
	 */
	private static final ThreadPoolExecutor FIXED_THREADPOOL = new ThreadPoolExecutor(200, 200, 0,
			TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1000), new NamedThreadFactory("dispatch-job"));

	private JobFacadeManager jobFacadeManager;
	private WorkerRemoteService jobRemoteService;

	public JobReceiver(JobFacadeManager jobFacadeManager, WorkerRemoteService jobRemoteService) {
		this.jobFacadeManager = jobFacadeManager;
		this.jobRemoteService = jobRemoteService;
	}

	/**
	 * 
	 * @param dto
	 * @return 成功时，也可能有ErrorCodeException，此时说明dispatch失败；失败时的ErrorCodeException是整个失败原因
	 */
	public Result2<ExecutableJobBO, ErrorCodeException> receive(CreateJobDTO dto) {
		ExecutableJobBO job;
		try {
			job = jobFacadeManager.create(dto);
		} catch (IllegalArgumentException e) {
			// 这个catch目前没有也会正确处理了
			return Results.of(false, null,
					new ClientParameterInvalidErrorCodeException("client.invalid-parameter", e.getMessage()));
		}

		try {
			MetricsInstance instance = jobRemoteService.enQueue(job);
			RegisteredInstance registeredInstance = instance.getAvailable();
			if (registeredInstance != null) {// 不可能是null
				/**
				 * 设置字段给controller使用;不需要更新queuedAtInstance到数据库，该字段是在worker enQueue成功后更新的
				 */
				job.setQueuedAt(SystemUtils.now());
				job.setQueuedAtInstance(
						SystemUtils.formatIpPort(registeredInstance.getIp(), registeredInstance.getPort()));
			}
			return Results.of(true, job, null);
		} catch (ExchangeException e) {
			log.warn("exchange failed on dispatch job after receive, job:{}", job, e);
			// dispatch失败不影响任务的成功接收
			return Results.of(true, job, new ServerErrorCodeException("dispatch-job-after-receive", e.getMessage(), e));
		}
	}

	/**
	 * 
	 * @param dto
	 * @return 成功时，也可能有ErrorCodeException，此时说明dispatch失败；失败时的ErrorCodeException是整个失败原因
	 */
	public Result2<ExecutableJobBO, ErrorCodeException> receiveAsync(CreateJobDTO dto) {
		ExecutableJobBO job;
		try {
			job = jobFacadeManager.create(dto);
		} catch (IllegalArgumentException e) {
			// 这个catch目前没有也会正确处理了
			return Results.of(false, null,
					new ClientParameterInvalidErrorCodeException("client.invalid-parameter", e.getMessage()));
		}

		try {
			FIXED_THREADPOOL.execute(() -> {
				try {
					jobRemoteService.enQueue(job);
				} catch (ExchangeException e) {
					log.warn("exchange failed on dispatch job after receive, job:{}", job, e);
				}
			});
			return Results.of(true, job, null);
		} catch (RejectedExecutionException ignore) {
			return Results.of(true, job,
					new ClientLimitedErrorCodeException("client.method-call-limited", "Limited:dispatch-job"));
		}
	}

}

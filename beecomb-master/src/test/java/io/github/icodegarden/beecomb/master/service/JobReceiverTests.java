package io.github.icodegarden.beecomb.master.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.CreateJobOpenapiDTO;
import io.github.icodegarden.nutrient.exchange.loadbalance.DefaultMetricsInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class JobReceiverTests {

	JobReceiver jobReceiver;
	JobFacadeManager jobFacadeManager;
	WorkerRemoteService jobRemoteService;

	@BeforeEach
	void init() {
		jobFacadeManager = mock(JobFacadeManager.class);
		jobRemoteService = mock(WorkerRemoteService.class);
		jobReceiver = new JobReceiver(jobFacadeManager, jobRemoteService);
	}

	@Test
	void receive() {
		CreateJobOpenapiDTO delayJobDTO = new CreateJobOpenapiDTO();
		delayJobDTO.setName("myjob1");
		delayJobDTO.setType(JobType.Delay);

		// 必须mock return
		doReturn(new DefaultMetricsInstance(null, null)).when(jobRemoteService).enQueue(any());

		jobReceiver.receive(delayJobDTO);
		verify(jobFacadeManager, times(1)).create(delayJobDTO);
		verify(jobRemoteService, times(1)).enQueue(any());
	}

	@Test
	void receiveAsync() throws InterruptedException {
		CreateJobOpenapiDTO delayJobDTO = new CreateJobOpenapiDTO();
		delayJobDTO.setName("myjob1");
		delayJobDTO.setType(JobType.Delay);

		jobReceiver.receiveAsync(delayJobDTO);
		Thread.sleep(100);
		verify(jobFacadeManager, times(1)).create(delayJobDTO);
		verify(jobRemoteService, times(1)).enQueue(any());
	}
}

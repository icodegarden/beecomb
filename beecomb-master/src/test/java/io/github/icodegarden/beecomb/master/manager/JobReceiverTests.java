package io.github.icodegarden.beecomb.master.manager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.master.manager.JobDispatcher;
import io.github.icodegarden.beecomb.master.manager.JobReceiver;
import io.github.icodegarden.beecomb.master.manager.JobStorage;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.commons.exchange.loadbalance.MetricsInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class JobReceiverTests {

	JobReceiver jobReceiver;
	JobStorage JobStorage;
	JobDispatcher jobDispatcher;

	@BeforeEach
	void init() {
		JobStorage = mock(JobStorage.class);
		jobDispatcher = mock(JobDispatcher.class);
		jobReceiver = new JobReceiver(JobStorage, jobDispatcher);
	}

	@Test
	void receive() {
		CreateJobDTO delayJobDTO = new CreateJobDTO();
		delayJobDTO.setName("myjob1");
		delayJobDTO.setType(JobType.Delay);

		// 必须mock return
		doReturn(new MetricsInstance.Default(null, null)).when(jobDispatcher).dispatch(any());

		jobReceiver.receive(delayJobDTO);
		verify(JobStorage, times(1)).create(delayJobDTO);
		verify(jobDispatcher, times(1)).dispatch(any());
	}

	@Test
	void receiveAsync() throws InterruptedException {
		CreateJobDTO delayJobDTO = new CreateJobDTO();
		delayJobDTO.setName("myjob1");
		delayJobDTO.setType(JobType.Delay);

		jobReceiver.receiveAsync(delayJobDTO);
		Thread.sleep(100);
		verify(JobStorage, times(1)).create(delayJobDTO);
		verify(jobDispatcher, times(1)).dispatch(any());
	}
}

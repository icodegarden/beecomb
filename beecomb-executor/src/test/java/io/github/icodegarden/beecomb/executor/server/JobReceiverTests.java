package io.github.icodegarden.beecomb.executor.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.beecomb.executor.registry.JobHandlerRegistry;
import io.github.icodegarden.nutrient.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.nutrient.lang.metricsregistry.MetricsOverload;
import io.github.icodegarden.nutrient.lang.result.Result2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class JobReceiverTests {

	JobHandlerRegistry jobHandlerRegistry;
	MetricsOverload jobOverload;
	JobReceiver jobReceiver;

	@BeforeEach
	void init() {
		jobHandlerRegistry = mock(JobHandlerRegistry.class);
		jobOverload = mock(MetricsOverload.class);
		jobReceiver = new JobReceiver(jobHandlerRegistry, jobOverload);
	}

	@Test
	void receive_no_jobHandler() throws Exception {
		// JobHandlerRegistry没有jobHandler
		Job job = mock(Job.class);
		Result2<ExecuteJobResult, ExchangeFailedReason> result2 = jobReceiver.receive(job);
		assertThat(result2.getT1()).isNull();
		assertThat(result2.isSuccess()).isFalse();
		assertThat(result2.getT2().getKeyword()).isEqualTo(ExchangeFailedReason.KEYWORD_SERVER_REJECTED);
		assertThat(result2.getT2().getDesc()).isEqualTo("No JobHandler");
	}

	@Test
	void receive_overload() throws Exception {
		// JobHandlerRegistry返回jobHandler
		doReturn(new MyJobHandler("ignore")).when(jobHandlerRegistry).getJobHandler(Mockito.any());
		// overload不允许

		Job job = mock(Job.class);
		Result2<ExecuteJobResult, ExchangeFailedReason> result2 = jobReceiver.receive(job);
		assertThat(result2.getT1()).isNull();
		assertThat(result2.isSuccess()).isFalse();
		assertThat(result2.getT2().getKeyword()).isEqualTo(ExchangeFailedReason.KEYWORD_SERVER_REJECTED);
		assertThat(result2.getT2().getDesc()).isEqualTo("Exceed Overload");

		Mockito.verify(jobOverload, Mockito.times(1)).incrementOverload(Mockito.any());// 1次
		Mockito.verify(jobOverload, Mockito.times(0)).decrementOverload(Mockito.any());// 0次
	}

	@Test
	void receive_handle_failed() throws Exception {
		// JobHandlerRegistry返回失败的jobHandler
		doReturn(new MyJobHandlerFailed("ignore")).when(jobHandlerRegistry).getJobHandler(Mockito.any());
		// 模拟overload允许
		doReturn(true).when(jobOverload).incrementOverload(Mockito.any());

		Job job = mock(Job.class);
		Result2<ExecuteJobResult, ExchangeFailedReason> result2 = jobReceiver.receive(job);
		assertThat(result2.getT1()).isNull();
		assertThat(result2.isSuccess()).isFalse();
		assertThat(result2.getT2().getKeyword()).isEqualTo(ExchangeFailedReason.KEYWORD_SERVER_EXCEPTION);
		assertThat(result2.getT2().getDesc()).isNotNull();

		Mockito.verify(jobOverload, Mockito.times(1)).incrementOverload(Mockito.any());// 1次
		Mockito.verify(jobOverload, Mockito.times(1)).decrementOverload(Mockito.any());// 1次
	}

	@Test
	void receive_ok() throws Exception {
		// JobHandlerRegistry返回jobHandler
		doReturn(new MyJobHandler("ignore")).when(jobHandlerRegistry).getJobHandler(Mockito.any());
		// 模拟overload允许
		doReturn(true).when(jobOverload).incrementOverload(Mockito.any());

		Job job = mock(Job.class);
		Result2<ExecuteJobResult, ExchangeFailedReason> result2 = jobReceiver.receive(job);
		assertThat(result2.isSuccess()).isTrue();
		assertThat(result2.getT2()).isNull();

		Mockito.verify(jobOverload, Mockito.times(1)).incrementOverload(Mockito.any());// 1次
		Mockito.verify(jobOverload, Mockito.times(1)).decrementOverload(Mockito.any());// 1次
	}

	private class MyJobHandler implements JobHandler {
		private String name;

		public MyJobHandler(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public ExecuteJobResult handle(Job job) {
			return null;
		}
	}

	private class MyJobHandlerFailed implements JobHandler {
		private String name;

		public MyJobHandlerFailed(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public ExecuteJobResult handle(Job job) {
			throw new RuntimeException("failed");
		}
	}
}

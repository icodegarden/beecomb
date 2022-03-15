package io.github.icodegarden.beecomb.executor.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.commons.exchange.InstanceExchangeResult;
import io.github.icodegarden.commons.exchange.ParallelShardObject;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.lang.result.Result2;
import io.github.icodegarden.commons.nio.MessageHandler;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ExecuteMessageHandler implements MessageHandler {
	private static final Logger log = LoggerFactory.getLogger(ExecuteMessageHandler.class);

	private final JobReceiver jobReceiver;

	public ExecuteMessageHandler(JobReceiver jobReceiver) {
		this.jobReceiver = jobReceiver;
	}

	@Override
	public Object reply(Object obj) {
		//TODO 需要能够处理各种类型的消息并做可靠性，不能只识别job
		
		Job job;
		if (obj instanceof ParallelShardObject) {
			ParallelShardObject parallelShardObject = ((ParallelShardObject) obj);
			job = (Job) parallelShardObject.getObj();
			job.setShard(parallelShardObject.getShard());
		} else {
			job = (Job) obj;
		}

		try {
			Result2<ExecuteJobResult, ExchangeFailedReason> result2 = jobReceiver.receive(job);
			if (!result2.isSuccess()) {
				log.warn("job was failed on receive, reason:{}", result2.getT2());
			}
			return InstanceExchangeResult.server(result2.isSuccess(), result2.getT1(), result2.getT2());
		} catch (Exception e) {
			// 不会抛出，担保
			log.error("ex on receive job:{}", job, e);
			return InstanceExchangeResult.server(false, null, ExchangeFailedReason.serverException(e.getMessage(), e));
		}
	}

	@Override
	public void receive(Object obj) {
	}
}

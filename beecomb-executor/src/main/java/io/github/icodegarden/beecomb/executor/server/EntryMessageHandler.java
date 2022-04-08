package io.github.icodegarden.beecomb.executor.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.commons.exchange.InstanceExchangeResult;
import io.github.icodegarden.commons.exchange.ParallelShardObject;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.lang.result.Result2;
import io.github.icodegarden.commons.lang.result.Results;
import io.github.icodegarden.commons.nio.MessageHandler;

/**
 * 入口MessageHandler
 * 
 * @author Fangfang.Xu
 *
 */
public class EntryMessageHandler implements MessageHandler {
	private static final Logger log = LoggerFactory.getLogger(EntryMessageHandler.class);

	private final JobReceiver jobReceiver;

	public EntryMessageHandler(JobReceiver jobReceiver) {
		this.jobReceiver = jobReceiver;
	}

	@Override
	public Object reply(Object obj) {
		if (log.isDebugEnabled()) {
			log.debug("executor receive a reply obj:{}", obj);
		}
		Job job = null;
		if (obj instanceof ParallelShardObject) {
			ParallelShardObject parallelShardObject = ((ParallelShardObject) obj);
			if (parallelShardObject.getObj() != null && parallelShardObject.getObj() instanceof Job) {
				job = (Job) parallelShardObject.getObj();
				job.setShard(parallelShardObject.getShard());
			}
		} else {
			if (obj instanceof Job) {
				job = (Job) obj;
			}
		}

		try {
			Result2<ExecuteJobResult, ExchangeFailedReason> result2 = null;
			if (job != null) {
				result2 = jobReceiver.receive(job);
				if (!result2.isSuccess()) {
					log.warn("receive job failed, reason:{}", result2.getT2());
				}
			} else {
				result2 = Results.of(true, null, null);
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

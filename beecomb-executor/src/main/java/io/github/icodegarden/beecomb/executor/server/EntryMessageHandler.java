package io.github.icodegarden.beecomb.executor.server;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.beecomb.common.executor.ShardObject;
import io.github.icodegarden.beecomb.common.pojo.transfer.RequestExecutorDTO;
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

	private volatile boolean closed;
	private AtomicLong processingCount = new AtomicLong(0);

	private final DispatcherHandler dispatcherHandler;

	public EntryMessageHandler(DispatcherHandler dispatcherHandler) {
		this.dispatcherHandler = dispatcherHandler;
	}

	@Override
	public Object reply(Object obj) {
		if (log.isInfoEnabled()) {
			log.info("Executor receive a reply obj:{}", obj);
		}

		if (closed) {
			return InstanceExchangeResult.server(false, null,
					ExchangeFailedReason.serverRejected("Executor Closed", null));
		}

		processingCount.incrementAndGet();

		try {
			RequestExecutorDTO dto = null;

			if (obj instanceof ParallelShardObject) {
				ParallelShardObject parallelShardObject = ((ParallelShardObject) obj);
				if (parallelShardObject.getObj() != null
						&& parallelShardObject.getObj() instanceof RequestExecutorDTO) {
					dto = (RequestExecutorDTO) parallelShardObject.getObj();
					Object body = dto.getBody();
					if (body instanceof ShardObject) {
						((ShardObject) body).setShard(parallelShardObject.getShard());
						((ShardObject) body).setShardTotal(parallelShardObject.getShardTotal());
					}
				}
			} else {
				if (!(obj instanceof RequestExecutorDTO)) {
					return null;
				}
				dto = (RequestExecutorDTO) obj;
			}

			Result2<Object, ExchangeFailedReason> result2 = null;
			if (dto != null) {
				if (dto.getBody() != null) {
					Method method = dispatcherHandler.getClass().getDeclaredMethod(dto.getMethod(),
							dto.getBody().getClass());
					/**
					 * 所有接口约定
					 */
					result2 = (Result2<Object, ExchangeFailedReason>) method.invoke(dispatcherHandler, dto.getBody());
				} else {
					Method method = dispatcherHandler.getClass().getDeclaredMethod(dto.getMethod());
					result2 = (Result2<Object, ExchangeFailedReason>) method.invoke(dispatcherHandler);
				}
			} else {
				result2 = Results.of(true, null, null);
			}

			if (!result2.isSuccess()) {
				log.warn("receive then handle obj failed, reason:{}", result2.getT2());
			}

			return InstanceExchangeResult.server(result2.isSuccess(), result2.getT1(), result2.getT2());
		} catch (Exception e) {
			// 不会抛出，担保
			log.error("ex on receive obj:{}", obj, e);
			return InstanceExchangeResult.server(false, null, ExchangeFailedReason.serverException(e.getMessage(), e));
		} finally {
			if (processingCount.decrementAndGet() <= 0) {
				synchronized (this) {
					this.notify();
				}
			}
		}
	}

	@Override
	public void receive(Object obj) {
	}

	/**
	 * 阻塞直到任务处理完毕或超时
	 * 
	 * @param blockTimeoutMillis
	 */
	public void closeBlocking(long blockTimeoutMillis) {
		closed = true;
		if (processingCount.get() > 0) {
			synchronized (this) {
				try {
					this.wait(blockTimeoutMillis);
				} catch (InterruptedException ignore) {
				}
			}
		}
	}
}

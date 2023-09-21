package io.github.icodegarden.beecomb.executor.server;

import java.lang.reflect.Method;

import io.github.icodegarden.beecomb.common.pojo.transfer.RequestExecutorDTO;
import io.github.icodegarden.commons.exchange.ReasonExchangeResult;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.lang.BodyObject;
import io.github.icodegarden.commons.lang.result.Result2;
import io.github.icodegarden.commons.nio.MessageHandler;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ExecutorMessageHandler implements MessageHandler<BodyObject, ReasonExchangeResult> {

	private final DispatcherHandler dispatcherHandler;

	public ExecutorMessageHandler(DispatcherHandler dispatcherHandler) {
		this.dispatcherHandler = dispatcherHandler;
	}

	@Override
	public ReasonExchangeResult reply(BodyObject bodyObject) {
		RequestExecutorDTO dto = (RequestExecutorDTO) bodyObject;

		Result2<Object, ExchangeFailedReason> result2 = null;
		try {
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

			return new ReasonExchangeResult(result2.isSuccess(), result2.getT1(), result2.getT2());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void receive(BodyObject bodyObject) {
	}

}

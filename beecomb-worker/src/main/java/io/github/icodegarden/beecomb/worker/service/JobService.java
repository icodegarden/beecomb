package io.github.icodegarden.beecomb.worker.service;

import io.github.icodegarden.beecomb.common.backend.service.BackendJobService;
import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteFailedDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteSuccessDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnNoQualifiedExecutorDTO;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.lang.result.Result1;
import io.github.icodegarden.commons.lang.result.Result2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobService extends BackendJobService {

	void updateEnQueue(ExecutableJobBO job);

	/**
	 * 对end的任务更新会返回成功，但数据不会变
	 * 
	 * @param update
	 * @return Boolean
	 *         NotNull,是否达到失败阈值，是则会end任务，当!success时null。RuntimeException：是否有异常，当更新不成功时可能由于id不匹配，可能由于异常
	 */
	Result2<Boolean, RuntimeException> updateOnNoQualifiedExecutor(UpdateOnNoQualifiedExecutorDTO update);

	/**
	 * 对end的任务更新会返回成功，但数据不会变
	 * 
	 * @param update
	 * @return RuntimeException：是否有异常，当更新不成功时可能由于id不匹配，可能由于异常
	 */
	Result1<RuntimeException> updateOnExecuteSuccess(UpdateOnExecuteSuccessDTO update);

	/**
	 * 如果原因不是{@link ExchangeException}，直接设置为已到阈值，结束任务.因为这种情况是系统故障，无法恢复，重试结果也是一样的<br>
	 * 对end的任务更新会返回成功，但数据不会变
	 * 
	 * @param update
	 * @return Boolean
	 *         NotNull,是否达到失败阈值，是则会end任务，当!success时null。RuntimeException：是否有异常，当更新不成功时可能由于id不匹配，可能由于异常
	 */
	Result2<Boolean, RuntimeException> updateOnExecuteFailed(UpdateOnExecuteFailedDTO update);
}

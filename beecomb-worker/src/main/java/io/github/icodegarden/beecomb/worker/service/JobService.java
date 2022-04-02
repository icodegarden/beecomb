package io.github.icodegarden.beecomb.worker.service;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.worker.core.JobFreshParams;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.exception.NoQualifiedInstanceExchangeException;
import io.github.icodegarden.commons.lang.result.Result1;
import io.github.icodegarden.commons.lang.result.Result2;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JobService {

	void updateEnQueue(ExecutableJobBO job);

	/**
	 * 对end的任务更新会返回成功，但数据不会变
	 * 
	 * @param update
	 * @return Boolean
	 *         NotNull,是否达到失败阈值，是则会end任务，当!success时null。RuntimeException：是否有异常，当更新不成功时可能由于id不匹配，可能由于异常
	 */
	Result2<Boolean, RuntimeException> updateOnNoQualifiedExecutor(UpdateOnNoQualifiedExecutor update);

	/**
	 * 对end的任务更新会返回成功，但数据不会变
	 * 
	 * @param update
	 * @return RuntimeException：是否有异常，当更新不成功时可能由于id不匹配，可能由于异常
	 */
	Result1<RuntimeException> updateOnExecuteSuccess(UpdateOnExecuteSuccess update);

	/**
	 * 如果原因不是{@link ExchangeException}，直接设置为已到阈值，结束任务.因为这种情况是系统故障，无法恢复，重试结果也是一样的<br>
	 * 对end的任务更新会返回成功，但数据不会变
	 * 
	 * @param update
	 * @return Boolean
	 *         NotNull,是否达到失败阈值，是则会end任务，当!success时null。RuntimeException：是否有异常，当更新不成功时可能由于id不匹配，可能由于异常
	 */
	Result2<Boolean, RuntimeException> updateOnExecuteFailed(UpdateOnExecuteFailed update);

	@Builder
	@Data
	public static class UpdateOnNoQualifiedExecutor {
		@NonNull
		private Long jobId;
		@NonNull
		private LocalDateTime lastTrigAt;
		@NonNull
		private NoQualifiedInstanceExchangeException noQualifiedInstanceExchangeException;
		/**
		 * 对delay忽略该字段<br>
		 * 对schedule不可以null<br>
		 */
		private LocalDateTime nextTrigAt;

		private Consumer<JobFreshParams> callback;
	}

	@Builder
	@Data
	public static class UpdateOnExecuteSuccess {
		@NonNull
		private Long jobId;
		@NonNull
		private LocalDateTime lastTrigAt;
		@NonNull
		private String executorIp;
		@NonNull
		private Integer executorPort;
		private String lastExecuteReturns;
		/**
		 * 对delay忽略该字段<br>
		 * 对schedule可以null<br>
		 */
		private Boolean end;
		/**
		 * 对delay忽略该字段<br>
		 * 对schedule不可以null<br>
		 */
		private LocalDateTime nextTrigAt;

		private Consumer<JobFreshParams> callback;
	}

	@Builder
	@Data
	public static class UpdateOnExecuteFailed {
		@NonNull
		private Long jobId;
		@NonNull
		private LocalDateTime lastTrigAt;
		@NonNull
		private Exception exception;
		/**
		 * 对delay忽略该字段<br>
		 * 对schedule不可以null<br>
		 */
		private LocalDateTime nextTrigAt;

		private Consumer<JobFreshParams> callback;
	}
}

package io.github.icodegarden.beecomb.worker.core;

import java.time.LocalDateTime;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.annotation.NotNull;
import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JobFreshParams {

	@Nullable
	private final String lastExecuteExecutor;// 只在成功时有参数
	@Nullable
	private final String lastExecuteReturns;// 只在成功时有参数
	private final boolean lastExecuteSuccess;
	@NotNull
	private final LocalDateTime lastTrigAt;
	@NotNull
	private final String lastTrigResult;

	public JobFreshParams(@Nullable String lastExecuteExecutor, @Nullable String lastExecuteReturns,
			boolean lastExecuteSuccess, @NotNull LocalDateTime lastTrigAt, @NotNull String lastTrigResult) {
		Assert.notNull(lastTrigAt, "lastTrigAt must not null");
		Assert.notNull(lastTrigResult, "lastTrigResult must not null");
		
		this.lastExecuteExecutor = lastExecuteExecutor;
		this.lastExecuteReturns = lastExecuteReturns;
		this.lastExecuteSuccess = lastExecuteSuccess;
		this.lastTrigAt = lastTrigAt;
		this.lastTrigResult = lastTrigResult;
	}

	public String getLastExecuteExecutor() {
		return lastExecuteExecutor;
	}

	public String getLastExecuteReturns() {
		return lastExecuteReturns;
	}

	public boolean isLastExecuteSuccess() {
		return lastExecuteSuccess;
	}

	public LocalDateTime getLastTrigAt() {
		return lastTrigAt;
	}

	public String getLastTrigResult() {
		return lastTrigResult;
	}

	@Override
	public String toString() {
		return "JobFreshParams [lastExecuteExecutor=" + lastExecuteExecutor + ", lastExecuteReturns="
				+ lastExecuteReturns + ", lastExecuteSuccess=" + lastExecuteSuccess + ", lastTrigAt=" + lastTrigAt
				+ ", lastTrigResult=" + lastTrigResult + "]";
	}

}

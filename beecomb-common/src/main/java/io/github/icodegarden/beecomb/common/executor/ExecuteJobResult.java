package io.github.icodegarden.beecomb.common.executor;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.util.Assert;

import io.github.icodegarden.nutrient.lang.annotation.Length;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ToString
public class ExecuteJobResult implements Serializable {
	private static final long serialVersionUID = -6248807678193840548L;

	@Length(max = 65535)
	private String executeReturns;
	/**
	 * 任务是否结束
	 */
	private boolean end;
	/**
	 * 是否在并行任务的所有分片都成功时执行回调
	 */
	private boolean onParallelSuccessCallback;

	public String getExecuteReturns() {
		return executeReturns;
	}

	public void setExecuteReturns(@Length(max = 65535) String executeReturns) {
		if (executeReturns != null) {
			Assert.isTrue(executeReturns.length() <= 65535, "executeReturns must lte 65535");
		}
		this.executeReturns = executeReturns;
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	public boolean isOnParallelSuccessCallback() {
		return onParallelSuccessCallback;
	}

	public void setOnParallelSuccessCallback(boolean onParallelSuccessCallback) {
		this.onParallelSuccessCallback = onParallelSuccessCallback;
	}

	@Override
	public int hashCode() {
		return Objects.hash(end, executeReturns);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExecuteJobResult other = (ExecuteJobResult) obj;
		return end == other.end && Objects.equals(executeReturns, other.executeReturns);
	}

}

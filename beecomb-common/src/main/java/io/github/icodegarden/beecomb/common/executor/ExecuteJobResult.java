package io.github.icodegarden.beecomb.common.executor;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.annotation.Length;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ExecuteJobResult implements Serializable {
	private static final long serialVersionUID = -6248807678193840548L;

	@Length(max = 65535) 
	private String executeReturns;
	/**
	 * 任务是否结束
	 */
	private boolean end;

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

	@Override
	public String toString() {
		return "ExecuteJobResult [executeReturns=" + executeReturns + ", end=" + end + "]";
	}

}

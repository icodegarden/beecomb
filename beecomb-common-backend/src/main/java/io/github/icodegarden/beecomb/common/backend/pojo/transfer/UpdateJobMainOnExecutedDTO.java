package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

import java.time.LocalDateTime;
import java.util.Arrays;

import io.github.icodegarden.beecomb.common.Validateable;
import io.github.icodegarden.beecomb.common.util.ClassUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Setter
@Getter
@ToString
public class UpdateJobMainOnExecutedDTO implements Validateable {

	@NonNull
	private Long id;
	@NonNull
	private LocalDateTime lastTrigAt;
	/**
	 * 不一定有，如任务结束时
	 */
	private LocalDateTime nextTrigAt;
	@NonNull
	private String lastTrigResult;
	@NonNull
	private Boolean lastExecuteSuccess;
	/**
	 * 不一定有
	 */
	private String lastExecuteExecutor;
	/**
	 * 不一定有
	 */
	private String lastExecuteReturns;
	/**
	 * 不一定结束
	 */
	private Boolean end;
	/**
	 * 不一定有
	 */
	private Boolean queuedAtInstanceNull;

	@Override
	public void validate() throws IllegalArgumentException {
	}

	@Override
	public boolean shouldUpdate() {
		return ClassUtils.anyFieldHasValue(this, Arrays.asList("id"));
	}
}

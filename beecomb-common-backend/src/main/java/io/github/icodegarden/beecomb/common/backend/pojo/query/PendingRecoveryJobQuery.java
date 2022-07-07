package io.github.icodegarden.beecomb.common.backend.pojo.query;

import io.github.icodegarden.beecomb.common.pojo.query.BaseQuery;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class PendingRecoveryJobQuery extends BaseQuery {
	
	private With with;

	@Builder
	public PendingRecoveryJobQuery(int page, int size, String sort, String limit, With with) {
		super(page, size, sort, limit);
		this.with = with;
	}

	@Builder
	@Getter
	@ToString
	public static class With {
		private boolean priority;
		private boolean createdAt;
		private boolean updatedAt;

	}
}
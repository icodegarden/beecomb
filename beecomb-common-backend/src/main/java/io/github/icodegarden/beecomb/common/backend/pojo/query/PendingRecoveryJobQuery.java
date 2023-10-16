package io.github.icodegarden.beecomb.common.backend.pojo.query;

import io.github.icodegarden.nutrient.lang.query.BaseQuery;
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
	public PendingRecoveryJobQuery(int page, int size, String orderBy, With with) {
		super(page, size, orderBy);
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
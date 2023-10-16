package io.github.icodegarden.beecomb.master.pojo.query;

import io.github.icodegarden.nutrient.lang.query.BaseQuery;
import lombok.Builder;
import lombok.Data;
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
public class ReportLineQuery extends BaseQuery {

	private String type;

	private With with;

	@Builder
	public ReportLineQuery(int page, int size, String orderBy, String type, With with) {
		super(page, size, orderBy);
		this.type = type;
		this.with = with;
	}

	@Builder
	@Data
	public static class With {
		private boolean updatedAt;
	}
}
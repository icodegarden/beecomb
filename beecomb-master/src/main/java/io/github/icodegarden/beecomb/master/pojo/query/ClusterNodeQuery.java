package io.github.icodegarden.beecomb.master.pojo.query;

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
public class ClusterNodeQuery extends BaseQuery {

	private String serviceName;
	private String ip;

	@Builder
	public ClusterNodeQuery(int page, int size, String orderBy, String serviceName, String ip) {
		super(page, size, orderBy);
		this.serviceName = serviceName;
		this.ip = ip;
	}

}
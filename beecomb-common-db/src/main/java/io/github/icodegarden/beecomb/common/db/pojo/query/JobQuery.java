package io.github.icodegarden.beecomb.common.db.pojo.query;

import java.time.LocalDateTime;

import io.github.icodegarden.beecomb.common.enums.JobType;
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
public class JobQuery extends BaseQuery {

	private String uuid;
	private Boolean queued;
	private Boolean end;
	private String createdBy;
	private String nameLike;
	private JobType type;
	private Boolean parallel;
	private Boolean lastExecuteSuccess;
	private LocalDateTime createdAtGte;
	private LocalDateTime createdAtLte;
	private LocalDateTime lastTrigAtGte;
	private LocalDateTime lastTrigAtLte;

	/**
	 * 
	 */
	private LocalDateTime nextTrigAtLt;

	private JobWith with;

	@Builder
	public JobQuery(int page, int size, String sort, String limit, String uuid, Boolean queued, Boolean end,
			String createdBy, String nameLike, JobType type, Boolean parallel, Boolean lastExecuteSuccess,
			LocalDateTime createdAtGte, LocalDateTime createdAtLte, LocalDateTime lastTrigAtGte,
			LocalDateTime lastTrigAtLte, LocalDateTime nextTrigAtLt, JobWith with) {
		super(page, size, sort, limit);
		this.uuid = uuid;
		this.queued = queued;
		this.end = end;
		this.createdBy = createdBy;
		this.nameLike = nameLike;
		this.type = type;
		this.parallel = parallel;
		this.lastExecuteSuccess = lastExecuteSuccess;
		this.createdAtGte = createdAtGte;
		this.createdAtLte = createdAtLte;
		this.lastTrigAtGte = lastTrigAtGte;
		this.lastTrigAtLte = lastTrigAtLte;
		this.nextTrigAtLt = nextTrigAtLt;
		this.with = with;
	}

}

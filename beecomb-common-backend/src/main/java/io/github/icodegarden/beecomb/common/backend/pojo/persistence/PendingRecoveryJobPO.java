package io.github.icodegarden.beecomb.common.backend.pojo.persistence;

import java.time.LocalDateTime;

import io.github.icodegarden.nutrient.lang.util.SystemUtils;
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
@Data
public class PendingRecoveryJobPO {

	private Long jobId;
	private Integer priority;
	private LocalDateTime createdAt;// timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	private LocalDateTime updatedAt;// timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

	@Builder
	@Setter
	@Getter
	@ToString
	public static class InsertSelect {

		private LocalDateTime nextTrigAtLt;
		private String queuedAtInstance;
		private final LocalDateTime dt = SystemUtils.now();
	}
}

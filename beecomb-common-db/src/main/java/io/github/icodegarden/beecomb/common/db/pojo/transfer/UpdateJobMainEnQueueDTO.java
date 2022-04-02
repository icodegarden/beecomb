package io.github.icodegarden.beecomb.common.db.pojo.transfer;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Builder
@Getter
@ToString
public class UpdateJobMainEnQueueDTO {

	@NonNull
	private Long id;
	@NonNull
	private String queuedAtInstance;
	@NonNull
	private LocalDateTime nextTrigAt;

}

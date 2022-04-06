package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

import java.time.LocalDateTime;

import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.common.Validateable;
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
public class UpdateJobMainEnQueueDTO implements Validateable {

	@NonNull
	private Long id;
	@NonNull
	private String queuedAtInstance;
	@NonNull
	private LocalDateTime nextTrigAt;

	@Override
	public void validate() throws IllegalArgumentException {
		Assert.notNull(id, "id");
		Assert.hasText(queuedAtInstance, "queuedAtInstance");
		Assert.notNull(nextTrigAt, "nextTrigAt");
	}

}

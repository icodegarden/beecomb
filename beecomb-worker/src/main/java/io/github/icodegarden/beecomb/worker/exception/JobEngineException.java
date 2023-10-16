package io.github.icodegarden.beecomb.worker.exception;

import java.util.Objects;

import io.github.icodegarden.nutrient.lang.annotation.NotNull;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class JobEngineException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	@NotNull
	private String reason;

	public JobEngineException(@NotNull String message) {
		this(message, null);
	}

	public JobEngineException(@NotNull String message, Throwable cause) {
		super(message, cause);
		Objects.requireNonNull(message, "message must not null");
		this.reason = message;
	}

	public String getReason() {
		return reason;
	}
}

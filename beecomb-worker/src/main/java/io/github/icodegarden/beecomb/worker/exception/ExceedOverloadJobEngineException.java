package io.github.icodegarden.beecomb.worker.exception;

import io.github.icodegarden.nutrient.lang.metricsregistry.Metrics;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ExceedOverloadJobEngineException extends JobEngineException {
	private static final long serialVersionUID = 1L;

	private Metrics metrics;

	public ExceedOverloadJobEngineException(String message, Metrics metrics) {
		super("Exceed Overload - " + message);
		this.metrics = metrics;
	}

	@Override
	public String getMessage() {
		return "[metrics=" + metrics + ", message=" + super.getMessage() + "]";
	}

}
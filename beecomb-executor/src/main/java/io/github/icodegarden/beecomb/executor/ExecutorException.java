package io.github.icodegarden.beecomb.executor;
/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ExecutorException extends RuntimeException {
	private static final long serialVersionUID = -5213250077486962423L;

	public ExecutorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExecutorException(Throwable cause) {
		super(cause);
	}

	public ExecutorException(String message) {
		super(message);
	}

}

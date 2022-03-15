package io.github.icodegarden.beecomb.worker.exception;

/**
 * 无法预料
 * 
 * @author Fangfang.Xu
 *
 */
public class ExceedExpectedJobEngineException extends JobEngineException {
	private static final long serialVersionUID = 1L;

	public ExceedExpectedJobEngineException(Throwable cause) {
		super("Exceed Expected", cause);
	}

}
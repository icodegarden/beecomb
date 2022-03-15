package io.github.icodegarden.beecomb.worker.exception;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class WorkerException extends RuntimeException {
	private static final long serialVersionUID = -5213250077486962423L;

	private boolean workerClosed;

	public WorkerException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorkerException(Throwable cause) {
		super(cause);
	}

	public WorkerException(String message) {
		super(message);
	}

	public boolean isWorkerClosed() {
		return workerClosed;
	}

	public static WorkerException workerClosed() {
		WorkerException workerException = new WorkerException("Worker Closed");
		workerException.workerClosed = true;
		return workerException;
	}
}
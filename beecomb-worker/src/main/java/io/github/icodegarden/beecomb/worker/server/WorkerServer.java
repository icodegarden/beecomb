package io.github.icodegarden.beecomb.worker.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import io.github.icodegarden.beecomb.common.pojo.transfer.RequestWorkerDTO;
import io.github.icodegarden.beecomb.worker.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.worker.exception.WorkerException;
import io.github.icodegarden.commons.exchange.DefaultInstanceExchangeResult;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.netty.NettyNioServer;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class WorkerServer implements GracefullyShutdown {
//	private static final Logger log = LoggerFactory.getLogger(ExecutorServer.class);

	private AtomicLong processingCount = new AtomicLong(0);
	private volatile boolean closed;

	private final InstanceProperties instanceProperties;
	private final DispatcherHandler dispatcherHandler;
	private NioServer nioServer;

	/**
	 * start a new Server
	 * 
	 * @param executorName
	 * @param config
	 * @return
	 * @throws WorkerException
	 */
	public WorkerServer(InstanceProperties instanceProperties, DispatcherHandler dispatcherHandler) {
		try {
			this.instanceProperties = instanceProperties;
			this.dispatcherHandler = dispatcherHandler;

			startNioServer();

			GracefullyShutdown.Registry.singleton().register(this);
		} catch (Throwable e) {
			throw new WorkerException("ex on start worker", e);
		}
	}

	private void startNioServer() throws IOException {
		String bindIp = instanceProperties.getServer().getBindIp();
		int port = instanceProperties.getServer().getPort();

		nioServer = new NettyNioServer("Worker-NioServer", new InetSocketAddress(bindIp, port), new MessageHandler() {

			@Override
			public Object reply(Object obj) {
				if (log.isInfoEnabled()) {
					log.info("Worker server receive a reply obj {}", obj);//FIXME 修改为debug
				}

				if (!(obj instanceof RequestWorkerDTO)) {
					return null;
				}

				processingCount.incrementAndGet();

				RequestWorkerDTO dto = (RequestWorkerDTO) obj;

				DefaultInstanceExchangeResult exchangeResult = new DefaultInstanceExchangeResult();
				try {
					if (closed) {
						if (log.isWarnEnabled()) {
							log.warn("job was rejected on receive, Worker Closed");
						}
						throw WorkerException.workerClosed();
					}

					Object object;
					if (dto.getBody() != null) {
						Method method = dispatcherHandler.getClass().getDeclaredMethod(dto.getMethod(),
								dto.getBody().getClass());
						object = method.invoke(dispatcherHandler, dto.getBody());
					} else {
						Method method = dispatcherHandler.getClass().getDeclaredMethod(dto.getMethod());
						object = method.invoke(dispatcherHandler);
					}

					exchangeResult.setSuccess(true);
					exchangeResult.setResponse(object);
					return exchangeResult;
				} catch (Exception e) {
					if (log.isWarnEnabled()) {
						log.warn("ex on receive obj {}", obj, e);
					}
					exchangeResult.setSuccess(false);

					if (e instanceof WorkerException && ((WorkerException) e).isWorkerClosed()) {
						/**
						 * WorkerClosed时，希望master选择其他worker，因此不能是serverException
						 */
						ExchangeFailedReason reason = ExchangeFailedReason.serverRejected(e.getMessage(), e);
						exchangeResult.setFailedReason(reason);
					} else {
						ExchangeFailedReason reason = ExchangeFailedReason.serverException(
								e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
						exchangeResult.setFailedReason(reason);
					}
					return exchangeResult;
				} finally {
					if (processingCount.decrementAndGet() <= 0) {
						synchronized (WorkerServer.this) {
							WorkerServer.this.notify();
						}
					}
				}
			}

			@Override
			public void receive(Object obj) {
			}
		});

		nioServer.start();
	}

	/**
	 * 优雅停机<br>
	 * 正式执行关闭前，不再接收任务，并确保正在执行中的任务执行完毕并且正常响应，最后关闭server
	 */
	public void close() throws IOException {
		closeBlocking(instanceProperties.getServer().getNioServerShutdownBlockingTimeoutMillis());

		nioServer.close();
	}

	/**
	 * 阻塞直到任务处理完毕或超时
	 * 
	 * @param blockTimeoutMillis
	 */
	private void closeBlocking(long blockTimeoutMillis) {
		closed = true;
		if (processingCount.get() > 0) {
			synchronized (WorkerServer.this) {
				try {
					WorkerServer.this.wait(blockTimeoutMillis);
				} catch (InterruptedException ignore) {
				}
			}
		}
	}

	@Override
	public String shutdownName() {
		return "workerServer";
	}

	@Override
	public void shutdown() {
		/**
		 * 停止NioServer不再接收任务
		 */
		try {
			this.close();
		} catch (IOException e) {
			throw new WorkerException(e);
		}
	}

	@Override
	public int shutdownOrder() {
		return -90;
	}
}

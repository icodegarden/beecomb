package io.github.icodegarden.beecomb.worker.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
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

	private final InstanceProperties instanceProperties;
	private final JobReceiver jobReceiver;
	private NioServer nioServer;

	/**
	 * start a new Server
	 * 
	 * @param executorName
	 * @param config
	 * @return
	 * @throws WorkerException
	 */
	public WorkerServer(InstanceProperties instanceProperties, JobReceiver jobReceiver) {
		try {
			this.instanceProperties = instanceProperties;
			this.jobReceiver = jobReceiver;

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
				if (log.isDebugEnabled()) {
					log.debug("worker server receive a reply obj:{}", obj);
				}
				/**
				 * 适配其他请求
				 */
				if (!(obj instanceof ExecutableJobBO)) {
					return null;
				}

				ExecutableJobBO job = (ExecutableJobBO) obj;

				DefaultInstanceExchangeResult exchangeResult = new DefaultInstanceExchangeResult();
				try {
					jobReceiver.receive(job);
					exchangeResult.setSuccess(true);
				} catch (WorkerException e) {
					if (log.isWarnEnabled()) {
						log.warn("ex on receive job:{}", job, e);
					}
					exchangeResult.setSuccess(false);

					if (e.isWorkerClosed()) {
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
				}
				return exchangeResult;
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
		jobReceiver.closeBlocking(instanceProperties.getServer().getNioServerShutdownBlockingTimeoutMillis());

		nioServer.close();
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

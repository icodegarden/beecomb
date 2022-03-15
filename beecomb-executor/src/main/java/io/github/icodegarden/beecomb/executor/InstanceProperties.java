package io.github.icodegarden.beecomb.executor;

import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class InstanceProperties {

	private Server server = new Server();
	private Overload overload = new Overload();
	private Schedule schedule = new Schedule();

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public Overload getOverload() {
		return overload;
	}

	public void setOverload(Overload overload) {
		this.overload = overload;
	}

	@Override
	public String toString() {
		return "InstanceProperties [server=" + server + ", overload=" + overload + ", schedule=" + schedule + "]";
	}

	public static class Server {
		private String executorIp = SystemUtils.getIp();
		private int executorPort = 29898;
		/**
		 * 影响nioServer关闭时等待已接收处理中的任务完毕
		 */
		private long nioServerShutdownBlockingTimeoutMillis = 60000;

		public String getExecutorIp() {
			return executorIp;
		}

		public void setExecutorIp(String executorIp) {
			this.executorIp = executorIp;
		}

		public int getExecutorPort() {
			return executorPort;
		}

		public void setExecutorPort(int executorPort) {
			this.executorPort = executorPort;
		}

		public long getNioServerShutdownBlockingTimeoutMillis() {
			return nioServerShutdownBlockingTimeoutMillis;
		}

		public void setNioServerShutdownBlockingTimeoutMillis(long nioServerShutdownBlockingTimeoutMillis) {
			this.nioServerShutdownBlockingTimeoutMillis = nioServerShutdownBlockingTimeoutMillis;
		}

		@Override
		public String toString() {
			return "Server [executorIp=" + executorIp + ", executorPort=" + executorPort
					+ ", nioServerShutdownBlockingTimeoutMillis=" + nioServerShutdownBlockingTimeoutMillis + "]";
		}

	}

	/**
	 * 进程完全用作executor
	 * 
	 * @return
	 */
	public static Overload fullExecutorOverload() {
		Overload overload = new Overload();
		overload.getJobs().setMax((int) SystemUtils.getVmRuntime().maxConcurrentThreadsPerSecond());
		return overload;
	}

	/**
	 * 进程既作为业务系统，又作为executor
	 * 
	 * @return
	 */
	public static Overload bizMixedExecutorOverload() {
		Overload overload = new Overload();
		return overload;
	}

	public static class Overload {
		/**
		 * 默认不需要开启cpu、memory，因为jobs是通过cpu核memory综合计算得出的，只使用jobs具有了cpu和memory的上限能力
		 */
		private Cpu cpu;
		private Memory memory;
		private Jobs jobs = new Jobs();

		public Cpu getCpu() {
			return cpu;
		}

		public void setCpu(Cpu cpu) {
			this.cpu = cpu;
		}

		public Memory getMemory() {
			return memory;
		}

		public void setMemory(Memory memory) {
			this.memory = memory;
		}

		public Jobs getJobs() {
			return jobs;
		}

		public void setJobs(Jobs jobs) {
			this.jobs = jobs;
		}

		public static class Cpu {
			private int weight = 1;

			public int getWeight() {
				return weight;
			}

			public void setWeight(int weight) {
				this.weight = weight;
			}
		}

		public static class Memory {
			private int weight = 1;

			public int getWeight() {
				return weight;
			}

			public void setWeight(int weight) {
				this.weight = weight;
			}
		}

		public static class Jobs {
			private int weight = 8;
			/**
			 * 默认executor是业务混合的，1/8用于executor
			 */
			private int max = (int) SystemUtils.getVmRuntime().maxConcurrentThreadsPerSecond() / 8;

			public void setWeight(int weight) {
				this.weight = weight;
			}

			public void setMax(int max) {
				this.max = max;
			}

			public int getWeight() {
				return weight;
			}

			public int getMax() {
				return max;
			}
		}
	}

	public static class Schedule {
		/**
		 * 
		 */
		private int flushMetricsIntervalMillis = 1000;

		public int getFlushMetricsIntervalMillis() {
			return flushMetricsIntervalMillis;
		}

		public void setFlushMetricsIntervalMillis(int flushMetricsIntervalMillis) {
			this.flushMetricsIntervalMillis = flushMetricsIntervalMillis;
		}

		@Override
		public String toString() {
			return "Schedule [flushMetricsIntervalMillis=" + flushMetricsIntervalMillis + "]";
		}

	}
}
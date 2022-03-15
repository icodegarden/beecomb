package io.github.icodegarden.beecomb.worker.configuration;

import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
public class LifecycleConfiguration implements SmartLifecycle {

	private boolean running;

	@Override
	public void start() {
		running = true;
	}

	@Override
	public void stop() {
		running = false;

		GracefullyShutdown.Registry.singleton().shutdownRegistered();
	}

	@Override
	public boolean isRunning() {
		return running;
	}

}
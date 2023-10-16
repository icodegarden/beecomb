package io.github.icodegarden.beecomb.master.discovery;

import io.github.icodegarden.nutrient.lang.metricsregistry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface InstanceDiscoveryListener {

	default void onInstanceCreated(RegisteredInstance registeredInstance) {

	}

	default void onInstanceDeleted(RegisteredInstance registeredInstance) {

	}
}

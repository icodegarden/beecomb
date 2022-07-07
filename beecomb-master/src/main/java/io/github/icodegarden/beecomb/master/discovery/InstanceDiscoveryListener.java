package io.github.icodegarden.beecomb.master.discovery;

import io.github.icodegarden.commons.lang.registry.RegisteredInstance;

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

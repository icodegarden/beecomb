package io.github.icodegarden.beecomb.common.backend.executor.registry;

import java.util.List;

import io.github.icodegarden.commons.lang.metricsregistry.InstanceDiscovery;

/**
 * 返回的实例注册信息附带JobHandler信息
 * 
 * @author Fangfang.Xu
 *
 */
public interface ExecutorInstanceDiscovery<T extends ExecutorRegisteredInstance> extends InstanceDiscovery<T> {

//	@Override
//	default List<T> listInstances(String ignore) {
//		return listNamedObjects(ignore);
//	}
}

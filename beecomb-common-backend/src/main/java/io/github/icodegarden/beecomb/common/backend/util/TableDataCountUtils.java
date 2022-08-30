package io.github.icodegarden.beecomb.common.backend.util;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import io.github.icodegarden.commons.lang.query.TableDataCountManager;
import io.github.icodegarden.commons.springboot.SpringContext;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class TableDataCountUtils {

	public static boolean allowCount(String tableName) {
		try {
			TableDataCountManager tableDataCountManager = SpringContext.getApplicationContext()
					.getBean(TableDataCountManager.class);
			return tableDataCountManager.allowCount(tableName);
		} catch (NoSuchBeanDefinitionException e) {
			return true;
		}
	}
}
package io.github.icodegarden.beecomb.master.service;

import java.lang.reflect.Field;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
public class SpringBeans implements ApplicationContextAware {

	private static ApplicationContext ac;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		try {
			Field field = SpringBeans.class.getDeclaredField("ac");
			field.set(null, applicationContext);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static <T> T getBean(Class<T> requiredType) {
		return ac.getBean(requiredType);
	}

	public static <T> T getBean(String name, Class<T> requiredType) {
		return ac.getBean(name, requiredType);
	}

	public static <T> Map<String, T> getBeansOfType(Class<T> requiredType) {
		return ac.getBeansOfType(requiredType);
	}
}
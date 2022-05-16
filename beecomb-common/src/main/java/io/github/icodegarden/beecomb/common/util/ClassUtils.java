package io.github.icodegarden.beecomb.common.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ClassUtils {

	/**
	 * 任意字段是否有值
	 * @param obj
	 * @param ignoreFieldNames 忽略的字段
	 * @return
	 */
	public static boolean anyFieldHasValue(Object obj, Collection<String> ignoreFieldNames) {
		Class<?> cla = obj.getClass();
		List<Field> allDeclaredFields = getAllDeclaredFields(cla);

		return allDeclaredFields.stream().anyMatch(field -> {
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			try {
				return !ignoreFieldNames.contains(field.getName()) && field.get(obj) != null;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("ex on doShouldUpdate", e);
			} finally {
				field.setAccessible(accessible);
			}
		});
	}

	/**
	 * 包含所有父类的字段
	 * @param cla
	 * @return
	 */
	public static List<Field> getAllDeclaredFields(Class<?> cla) {
		List<Field> allDeclaredFields = new LinkedList<Field>();
		while (cla != null && cla != Object.class) {
			Field[] declaredFields = cla.getDeclaredFields();
			allDeclaredFields.addAll(Arrays.asList(declaredFields));

			cla = cla.getSuperclass();
		}
		return allDeclaredFields;
	}
}

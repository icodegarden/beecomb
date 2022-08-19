package io.github.icodegarden.beecomb.common.backend.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.backend.manager.TableDataCountManager;
import io.github.icodegarden.commons.springboot.SpringContext;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class PageHelperUtils {

	public static boolean allowCount(String tableName) {
		try {
			TableDataCountManager tableDataCountManager = SpringContext.getApplicationContext()
					.getBean(TableDataCountManager.class);
			return tableDataCountManager.allowCount(tableName);
		} catch (NoSuchBeanDefinitionException e) {
			return true;
		}
	}

	/**
	 * 分页的按正常处理<br>
	 * 不分页但结果条数小于页大小，则总页数按当前页处理，总条数按(总页数-1)*每页大小+当前返回条数<br>
	 * 不分页但结果条数等于页大小，则按最大值处理
	 */
	public static <E, T> Page<E> ofPage(Page<T> page, Function<T, E> elementConvertor) {
		Page<E> p = new Page<E>(page.getPageNum(), page.getPageSize());
		if (page.isCount()) {
			p.setTotal(page.getTotal());
			p.setPages(page.getPages());
		} else {
			if (page.getResult().size() < page.getPageSize()) {
				p.setTotal((p.getPages() - 1) * page.getPageSize() + page.getResult().size());
				p.setPages(page.getPageNum());
			} else {
				p.setTotal(10000);
				p.setPages(10000/page.getPageSize());
			}
		}

		if (!page.getResult().isEmpty()) {
			List<E> list = page.getResult().stream().map(t -> {
				return elementConvertor.apply(t);
			}).collect(Collectors.toList());

			p.addAll(list);
		}

		return p;
	}
}

package io.github.icodegarden.beecomb.common.backend.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.common.backend.manager.TableDataCountManager;
import io.github.icodegarden.commons.springboot.SpringContext;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

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
			p.setPages(page.getPages());
			p.setTotal(page.getTotal());
		} else {
			if (page.getResult().size() < page.getPageSize()) {
				p.setPages(page.getPageNum());
				p.setTotal((p.getPages() - 1) * page.getPageSize() + page.getResult().size());
			} else {
				p.setPages(WebUtils.MAX_TOTAL_PAGES);
				p.setTotal(page.getPageSize() * WebUtils.MAX_TOTAL_PAGES);
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

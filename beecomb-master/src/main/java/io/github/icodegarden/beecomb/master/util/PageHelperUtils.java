package io.github.icodegarden.beecomb.master.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.pagehelper.Page;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class PageHelperUtils {

	public static <E, T> Page<E> ofPage(Page<T> page, Function<T, E> elementConvertor) {
		Page<E> p = new Page<E>(page.getPageNum(), page.getPageSize());
		p.setPages(page.getPages());
		p.setTotal(page.getTotal());

		if (!page.getResult().isEmpty()) {
			List<E> list = page.getResult().stream().map(t -> {
				return elementConvertor.apply(t);
			}).collect(Collectors.toList());

			p.addAll(list);
		}

		return p;
	}
}

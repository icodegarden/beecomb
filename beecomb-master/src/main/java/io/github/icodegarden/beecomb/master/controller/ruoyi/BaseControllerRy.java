package io.github.icodegarden.beecomb.master.controller.ruoyi;

import java.beans.PropertyEditorSupport;
import java.util.List;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;

import io.github.icodegarden.beecomb.master.ruoyi.AjaxResult;
import io.github.icodegarden.beecomb.master.ruoyi.TableDataInfo;
import io.github.icodegarden.commons.lang.query.BaseQuery;

/**
 * web层通用数据处理
 * 
 * @author ruoyi
 */
public abstract class BaseControllerRy {

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String value) {
				if (!StringUtils.hasText(value)) {
					setValue(null);
				} else {
					setValue(value);
				}
			}
		});
	}

	/**
	 * 响应请求分页数据
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected TableDataInfo getDataTable(List<?> list) {
		TableDataInfo rspData = new TableDataInfo();
		rspData.setCode(0);
		rspData.setRows(list);

		long total = new PageInfo(list).getTotal();
		if (list instanceof Page) {
			int pageSize = ((Page) list).getPageSize();
			int maxTotal = BaseQuery.MAX_TOTAL_PAGES * pageSize;
			if (total > maxTotal) {
				total = maxTotal;
			}
		}
		rspData.setTotal(total);
		return rspData;
	}

	/**
	 * 返回成功
	 */
	public AjaxResult success() {
		return AjaxResult.success();
	}

	/**
	 * 返回失败消息
	 */
	public AjaxResult error() {
		return AjaxResult.error();
	}
}

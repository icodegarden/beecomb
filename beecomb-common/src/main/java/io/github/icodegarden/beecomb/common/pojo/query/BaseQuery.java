package io.github.icodegarden.beecomb.common.pojo.query;

/**
 *
 * @author Fangfang.Xu
 *
 */
public abstract class BaseQuery {

	public static final int DEFAULT_SIZE = 10;

	private int page = 1;

	private int size = DEFAULT_SIZE;

	/**
	 * 需要完整的字符串：order by id desc
	 */
	private String sort;
	/**
	 * 需要完整的字符串：limit 10; limit 0,10 ; limit 10,10
	 */
	private String limit;

	public BaseQuery(int page, int size, String sort, String limit) {
		if(page <= 0) {
			page = 1;
		}
		if(size <= 0) {
			size = 1;
		}
		this.page = page;
		this.size = size;
		this.sort = sort;
		this.limit = limit;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	@Override
	public String toString() {
		return "BaseQuery [page=" + page + ", size=" + size + ", sort=" + sort + ", limit=" + limit + "]";
	}

}
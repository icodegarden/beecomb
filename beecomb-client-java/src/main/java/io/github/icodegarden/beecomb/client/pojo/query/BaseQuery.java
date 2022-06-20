package io.github.icodegarden.beecomb.client.pojo.query;

/**
 *
 * @author Fangfang.Xu
 *
 */
public abstract class BaseQuery {

	public static final int DEFAULT_SIZE = 10;

	private int page = 1;

	private int size = DEFAULT_SIZE;

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

	@Override
	public String toString() {
		return "BaseQuery [page=" + page + ", size=" + size + "]";
	}

}
package io.github.icodegarden.beecomb.client.pojo.response;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class PageResponse<T> {

	private int page;
	private int size;

	private int totalPages;
	private long totalCount;

	private List<T> result;
	
	public PageResponse() {
	}

	public PageResponse(int page, int size, int totalPages, long totalCount, List<T> result) {
		super();
		this.page = page;
		this.size = size;
		this.totalPages = totalPages;
		this.totalCount = totalCount;
		this.result = result;
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

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public List<T> getResult() {
		return result;
	}

	public void setResult(List<T> result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "PageResponse [page=" + page + ", size=" + size + ", totalPages=" + totalPages + ", totalCount="
				+ totalCount + ", result=" + result + "]";
	}

}

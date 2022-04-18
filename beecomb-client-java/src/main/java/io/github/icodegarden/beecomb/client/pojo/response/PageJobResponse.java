package io.github.icodegarden.beecomb.client.pojo.response;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class PageJobResponse {

	private int page;
	private int size;

	private int totalPages;
	private long totalCount;
	
	private List<GetJobResponse> jobs;

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

	public List<GetJobResponse> getJobs() {
		return jobs;
	}

	public void setJobs(List<GetJobResponse> jobs) {
		this.jobs = jobs;
	}

	@Override
	public String toString() {
		return "PageJobResponse [page=" + page + ", size=" + size + ", totalPages=" + totalPages + ", totalCount="
				+ totalCount + ", jobs=" + jobs + "]";
	}
	
}

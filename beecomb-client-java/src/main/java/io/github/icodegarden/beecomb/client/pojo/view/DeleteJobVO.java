package io.github.icodegarden.beecomb.client.pojo.view;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DeleteJobVO {

	private Long id;
	private Boolean success;

	public Long getId() {
		return id;
	}

	public Boolean getSuccess() {
		return success;
	}

	@Override
	public String toString() {
		return "DeleteJobVO [id=" + id + ", success=" + success + "]";
	}

}
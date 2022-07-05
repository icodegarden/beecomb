package io.github.icodegarden.beecomb.client.pojo.view;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class UpdateJobVO {

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
		return "UpdateJobVO [id=" + id + ", success=" + success + "]";
	}

}
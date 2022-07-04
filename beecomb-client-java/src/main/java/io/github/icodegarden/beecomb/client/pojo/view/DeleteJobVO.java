package io.github.icodegarden.beecomb.client.pojo.view;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DeleteJobVO {

	private Long jobId;
	private Boolean Success;

	public Long getJobId() {
		return jobId;
	}

	public Boolean getSuccess() {
		return Success;
	}

	@Override
	public String toString() {
		return "DeleteJobVO [jobId=" + jobId + ", Success=" + Success + "]";
	}

}
package io.github.icodegarden.beecomb.master.pojo.view.openapi;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.master.pojo.view.JobVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Setter
@Getter
@ToString
public class GetJobOpenapiVO extends PageJobsOpenapiVO {

	public static GetJobOpenapiVO of(JobVO one) {
		if (one == null) {
			return null;
		}
		GetJobOpenapiVO vo = new GetJobOpenapiVO();
		BeanUtils.copyProperties(one, vo);
		return vo;
	}
}

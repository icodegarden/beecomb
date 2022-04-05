package io.github.icodegarden.beecomb.master.pojo.view.openapi;

import org.springframework.beans.BeanUtils;

import io.github.icodegarden.beecomb.common.db.pojo.view.JobMainVO;
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
public class PageJobsOpenapiVO extends GetJobOpenapiVO {

	public static PageJobsOpenapiVO of(JobMainVO one) {
		if (one == null) {
			return null;
		}
		PageJobsOpenapiVO vo = new PageJobsOpenapiVO();
		BeanUtils.copyProperties(one, vo);
		if (one.getJobDetail() != null) {
			BeanUtils.copyProperties(one.getJobDetail(), vo);
		}
		if (one.getDelayJob() != null) {
			BeanUtils.copyProperties(one.getDelayJob(), vo);
		}
		if (one.getScheduleJob() != null) {
			BeanUtils.copyProperties(one.getScheduleJob(), vo);
		}

		return vo;
	}
}
package io.github.icodegarden.beecomb.master.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.commons.springboot.SpringContext;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Primary
@Service
public class PrimaryJobService extends AbstractJobService {

	@Override
	public ExecutableJobBO create(CreateJobDTO dto) throws IllegalArgumentException {
		String typeName = dto.getType().name().toLowerCase();
		JobService jobService = SpringContext.getApplicationContext().getBean(typeName + "JobService",
				JobService.class);

		return jobService.create(dto);
	}

}

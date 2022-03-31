package io.github.icodegarden.beecomb.master.manager;

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
public class PrimaryJobStorage extends BaseJobStorage implements JobStorage {

	@Override
	public ExecutableJobBO create(CreateJobDTO dto) throws IllegalArgumentException {
		String typeName = dto.getType().name().toLowerCase();
		JobStorage jobStorage = SpringContext.getApplicationContext().getBean(typeName, JobStorage.class);

		return jobStorage.create(dto);
	}

}

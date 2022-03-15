package io.github.icodegarden.beecomb.master.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.pojo.biz.ExecutableJobBO;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;

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
		JobStorage jobStorage = SpringBeans.getBean(typeName, JobStorage.class);

		return jobStorage.create(dto);
	}

}

package io.github.icodegarden.beecomb.common.db.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.db.mapper.JobMainMapper;
import io.github.icodegarden.beecomb.common.db.pojo.data.JobMainDO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO;
import io.github.icodegarden.beecomb.common.db.pojo.persistence.JobMainPO.Update;
import io.github.icodegarden.beecomb.common.db.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.db.pojo.transfer.UpdateJobMainEnQueueDTO;
import io.github.icodegarden.beecomb.common.db.pojo.transfer.UpdateJobMainOnExecutedDTO;
import io.github.icodegarden.beecomb.common.db.pojo.view.JobMainVO;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class JobMainManager {

	@Autowired
	private JobMainMapper jobMainMapper;

	public JobMainVO findOne(Long id, @Nullable JobMainQuery.With with) {
		JobMainDO one = jobMainMapper.findOne(id, with);
		return JobMainVO.of(one);
	}

	public boolean updateOnExecuted(UpdateJobMainOnExecutedDTO dto) {
		Update update = new JobMainPO.Update();
		BeanUtils.copyProperties(dto, update);

		return jobMainMapper.update(update) == 1;
	}

	public boolean updateEnQueue(UpdateJobMainEnQueueDTO dto) {
		Update update = new JobMainPO.Update();
		BeanUtils.copyProperties(dto, update);

		/**
		 * 设为已队列
		 */
		update.setQueued(true);
		update.setQueuedAt(SystemUtils.now());

		return jobMainMapper.update(update) == 1;
	}
}

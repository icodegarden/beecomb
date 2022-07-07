package io.github.icodegarden.beecomb.common.backend.manager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.backend.mapper.PendingRecoveryJobMapper;
import io.github.icodegarden.beecomb.common.backend.pojo.data.PendingRecoveryJobDO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.PendingRecoveryJobPO;
import io.github.icodegarden.beecomb.common.backend.pojo.persistence.PendingRecoveryJobPO.InsertSelect;
import io.github.icodegarden.beecomb.common.backend.pojo.query.PendingRecoveryJobQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.transfer.CreatePendingRecoveryJobDTO;
import io.github.icodegarden.beecomb.common.backend.pojo.view.PendingRecoveryJobVO;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class PendingRecoveryJobManager {

	@Autowired
	private PendingRecoveryJobMapper pendingRecoveryJobMapper;

	public void create(CreatePendingRecoveryJobDTO dto) {
		PendingRecoveryJobPO po = new PendingRecoveryJobPO();
		BeanUtils.copyProperties(dto, po);
		po.setCreatedAt(SystemUtils.now());
		po.setUpdatedAt(SystemUtils.now());

		pendingRecoveryJobMapper.add(po);
	}

//	public Page<PendingRecoveryJobVO> page(PendingRecoveryJobQuery query) {
//		PageHelper.startPage(query.getPage(), query.getSize());
//
//		Page<PendingRecoveryJobDO> page = (Page<PendingRecoveryJobDO>) pendingRecoveryJobMapper.findAll(query);
//
//		Page<PendingRecoveryJobVO> p = PageHelperUtils.ofPage(page, one -> PendingRecoveryJobVO.of(one));
//		return p;
//	}

	public List<PendingRecoveryJobVO> list(PendingRecoveryJobQuery query) {
		query.setLimitDefaultValueIfNotPresent();

		List<PendingRecoveryJobDO> list = pendingRecoveryJobMapper.findAll(query);

		return list.stream().map(DO -> PendingRecoveryJobVO.of(DO)).collect(Collectors.toList());
	}

//	public PendingRecoveryJobVO findOne(Long id, PendingRecoveryJobQuery.With with) {
//		PendingRecoveryJobDO one = pendingRecoveryJobMapper.findOne(id, with);
//		return PendingRecoveryJobVO.of(one);
//	}

	/**
	 * 增加待恢复，nextTrigAt超过给定的时间
	 * 
	 * @param nextTrigAtLt
	 * @return
	 */
	public int insertSelectByScan(LocalDateTime nextTrigAtLt) {
		InsertSelect insertSelect = PendingRecoveryJobPO.InsertSelect.builder().nextTrigAtLt(nextTrigAtLt).build();
		return pendingRecoveryJobMapper.insertSelectByScan(insertSelect);
	}

	/**
	 * 增加待恢复，所在实例实际已不存在的
	 * 
	 * @param queuedAtInstance
	 * @return
	 */
	public int insertSelectByInstance(String queuedAtInstance) {
		InsertSelect insertSelect = PendingRecoveryJobPO.InsertSelect.builder().queuedAtInstance(queuedAtInstance)
				.build();
		return pendingRecoveryJobMapper.insertSelectByInstance(insertSelect);
	}

	public void delete(Long jobId) {
		pendingRecoveryJobMapper.delete(jobId);
	}
}

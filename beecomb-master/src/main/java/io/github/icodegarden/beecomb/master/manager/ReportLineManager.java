package io.github.icodegarden.beecomb.master.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.icodegarden.beecomb.master.mapper.ReportLineMapper;
import io.github.icodegarden.beecomb.master.pojo.persistence.ReportLinePO;
import io.github.icodegarden.beecomb.master.pojo.query.ReportLineQuery;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateReportLineDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.UpdateReportLineDTO;
import io.github.icodegarden.nursery.springboot.exception.SQLConstraintException;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class ReportLineManager {

	@Autowired
	private ReportLineMapper reportLineMapper;

	public void create(CreateReportLineDTO dto) {
		ReportLinePO po = new ReportLinePO();
		BeanUtils.copyProperties(dto, po);
		po.setUpdatedAt(SystemUtils.now());
		try {
			reportLineMapper.add(po);
		} catch (DataIntegrityViolationException e) {
			throw new SQLConstraintException(e);
		}
	}

	public Page<ReportLinePO> page(ReportLineQuery query) {
		PageHelper.startPage(query.getPage(), query.getSize());

		Page<ReportLinePO> page = (Page<ReportLinePO>) reportLineMapper.findAll(query);
		return page;
	}

	public ReportLinePO findOne(Long id, ReportLineQuery.With with) {
		return reportLineMapper.findOne(id, with);
	}

	public ReportLinePO findOneByType(ReportLinePO.Type type, ReportLineQuery.With with) {
		return reportLineMapper.findOneByType(type.name(), with);
	}

	public void update(UpdateReportLineDTO dto) {
		ReportLinePO.Update update = new ReportLinePO.Update();
		BeanUtils.copyProperties(dto, update);

		reportLineMapper.update(update);
	}
}

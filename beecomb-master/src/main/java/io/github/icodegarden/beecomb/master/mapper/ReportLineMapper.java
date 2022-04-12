package io.github.icodegarden.beecomb.master.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.master.pojo.persistence.ReportLinePO;
import io.github.icodegarden.beecomb.master.pojo.query.ReportLineQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ReportLineMapper {

	void add(ReportLinePO po);

	List<ReportLinePO> findAll(ReportLineQuery query);

	ReportLinePO findOne(@Param("id") Long id, @Param("with") ReportLineQuery.With with);

	ReportLinePO findOneByType(@Param("type") String type, @Param("with") ReportLineQuery.With with);

	int update(ReportLinePO.Update update);
}

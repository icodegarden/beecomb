package io.github.icodegarden.beecomb.master.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.master.pojo.persistence.TableDataCountPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface TableDataCountMapper {
	
	void add(TableDataCountPO po);

	int updateCount(@Param("tableName") String tableName, @Param("count") long count);
	
	List<TableDataCountPO> findAll();
	
}

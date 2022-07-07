package io.github.icodegarden.beecomb.common.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface SqlMapper {

	String selectVersion();
	
	List<String> showTables();
	
	long countAll(@Param("tableName") String tableName);

	@Deprecated
	long nextId(@Param("moduleName") String moduleName);
	@Deprecated
	long currentId(@Param("moduleName") String moduleName);

}

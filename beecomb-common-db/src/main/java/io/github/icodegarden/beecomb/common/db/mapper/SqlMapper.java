package io.github.icodegarden.beecomb.common.db.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface SqlMapper {

	String selectVersion();

	long nextId(@Param("moduleName") String moduleName);
	
	long currentId(@Param("moduleName") String moduleName);

}

package io.github.icodegarden.beecomb.master.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.query.UserQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface UserMapper {

	void add(UserPO po);

	List<UserPO> findAll(UserQuery query);

	UserPO findOne(@Param("id") Long id, @Param("with") UserQuery.With with);

	UserPO findByUsername(@Param("username") String username, @Param("with") UserQuery.With with);

	int update(UserPO.Update update);
}

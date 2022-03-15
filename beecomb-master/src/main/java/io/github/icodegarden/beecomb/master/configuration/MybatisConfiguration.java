package io.github.icodegarden.beecomb.master.configuration;

import javax.annotation.PostConstruct;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.beecomb.common.db.mapper.SqlMapper;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
@MapperScan(basePackages = { "io.github.icodegarden.beecomb.common.db.mapper",
		"io.github.icodegarden.beecomb.master.mapper" })
public class MybatisConfiguration {

	@Autowired
	private SqlMapper sqlMapper;

	@PostConstruct
	void initPool() {
		sqlMapper.selectVersion();
	}
}

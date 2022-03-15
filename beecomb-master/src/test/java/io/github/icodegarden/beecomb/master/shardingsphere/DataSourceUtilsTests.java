package io.github.icodegarden.beecomb.master.shardingsphere;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootTest
class DataSourceUtilsTests {

	@Autowired
	DataSource dataSource;

	@Test
	void firstDataSource() {
		DataSource firstDataSource = DataSourceUtils.firstDataSource((ShardingSphereDataSource) dataSource);
		Assertions.assertThat(firstDataSource).isNotNull();
	}
}

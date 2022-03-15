package io.github.icodegarden.beecomb.master.shardingsphere;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.springframework.util.Assert;

import io.github.icodegarden.beecomb.master.util.SpringUtils;
import io.github.icodegarden.commons.lang.sequence.MysqlSequenceManager;
import io.github.icodegarden.commons.lang.sequence.SequenceManager;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlKeyGenerateAlgorithm implements KeyGenerateAlgorithm {

	private static final String MODULE_NAME_KEY = "moduleName";

	private Properties props = new Properties();

	private String moduleName;

	private volatile SequenceManager sequenceManager;

	@Override
	public void setProps(Properties props) {
		this.props = props;
	}

	@Override
	public void init() {
		moduleName = props.getProperty(MODULE_NAME_KEY);
		Assert.hasLength(moduleName, MODULE_NAME_KEY + " must not empty");
	}

	@Override
	public Comparable<?> generateKey() {
		if (sequenceManager == null) {
			ShardingSphereDataSource sdataSource = (ShardingSphereDataSource) SpringUtils.getBean(DataSource.class);
			DataSource dataSource = DataSourceUtils.firstDataSource(sdataSource);
			sequenceManager = new MysqlSequenceManager(moduleName, dataSource);
		}
		return sequenceManager.nextId();
	}

	@Override
	public String getType() {
		return "MYSQL";
	}
}

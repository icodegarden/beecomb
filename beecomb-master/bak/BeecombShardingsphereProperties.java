package io.github.icodegarden.beecomb.master;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.shardingsphere.builder.DataSourceConfig;
import io.github.icodegarden.commons.shardingsphere.builder.RangeModShardingAlgorithmConfig;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "api.shardingsphere")
public class BeecombShardingsphereProperties {

	private List<DataSourceConfig> datasources;
	private RangeModShardingAlgorithmConfig jobidrangemod = new RangeModShardingAlgorithmConfig();
}
package io.github.icodegarden.beecomb.common.backend.shardingsphere;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.shardingsphere.properties.DataSourceProperties;
import io.github.icodegarden.commons.shardingsphere.properties.RangeModProperties;
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

	private List<DataSourceProperties> datasources;
	private RangeModProperties jobidrangemod = new RangeModProperties();
}
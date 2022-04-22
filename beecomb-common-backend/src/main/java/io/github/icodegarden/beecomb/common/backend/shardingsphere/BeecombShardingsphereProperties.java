package io.github.icodegarden.beecomb.common.backend.shardingsphere;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.shardingsphere.algorithm.RangeModShardingAlgorithm;
import io.github.icodegarden.commons.shardingsphere.properties.Datasource;
import io.github.icodegarden.commons.shardingsphere.properties.Rangemod;
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

	private List<Datasource> datasources;
	private Rangemod jobidrangemod = new Rangemod();

	@PostConstruct
	void init() {
		jobidrangemod.validate();

		RangeModShardingAlgorithm.registerRangemod("jobidrangemod", jobidrangemod);
	}

}
package io.github.icodegarden.beecomb.master.shardingsphere;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.shardingsphere.rules.sharding.sharding-algorithms")
public class ShardingAlgorithmsProperties {

	private Algorithm idrangemod = new Algorithm();

	@Data
	public static class Algorithm {
		private String type;
		private Props props;

		@Data
		public static class Props {
			private String strategy;
			private String algorithmClassName;
			private List<Group> groups;

			@Data
			public static class Group {
				private String name;
				private Long rangeGte;
				private Long rangeLt;
				private Integer mod;
				private String modLoadBalance;// {"ds0":[0,1],"ds1":[2],"ds2":[3,4]}
			}
		}
	}

}
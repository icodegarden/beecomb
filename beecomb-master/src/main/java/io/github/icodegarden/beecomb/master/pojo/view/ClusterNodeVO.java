package io.github.icodegarden.beecomb.master.pojo.view;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class ClusterNodeVO {

	private String serviceName;

	private String instanceName;

	private String ip;

	private int port;

	private List<MetricsDimension> metricsDimensions;

	public ClusterNodeVO(String serviceName, String instanceName, String ip, int port,
			List<MetricsDimension> metricsDimensions) {
		this.serviceName = serviceName;
		this.instanceName = instanceName;
		this.ip = ip;
		this.port = port;
		this.metricsDimensions = metricsDimensions;
	}

	@Data
	public static class MetricsDimension {
		private String name;
		private double max;
		private double used;
		private int weight;
		private String desc;

		public MetricsDimension(String name, double max, double used, int weight, String desc) {
			this.name = name;
			this.max = max;
			this.used = used;
			this.weight = weight;
			this.desc = desc;
		}

	}
}

package io.github.icodegarden.beecomb.test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class PropertiesConfig {
	protected static final int MAX_JOBS_OVERLOAD = 2;
	static {
		System.setProperty("overload.jobs.max", MAX_JOBS_OVERLOAD + "");// 关系到测试
	}
	
	protected static String zkConnectString = "192.168.80.128:2181";
}

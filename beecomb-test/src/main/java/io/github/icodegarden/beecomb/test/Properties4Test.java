package io.github.icodegarden.beecomb.test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class Properties4Test {
	protected static final int MAX_JOBS_OVERLOAD = 2;
	static {
		System.setProperty("overload.jobs.max", MAX_JOBS_OVERLOAD + "");// 关系到测试
	}
	
//	public static String zkConnectString = System.getProperty("zkConnectString", "172.22.122.26:2181");//192.168.80.128:2181
	public static String zkConnectString = System.getProperty("zkConnectString", "127.0.0.1:2181");
}

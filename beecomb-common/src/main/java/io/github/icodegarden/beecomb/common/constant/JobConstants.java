package io.github.icodegarden.beecomb.common.constant;
/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class JobConstants {

	public static final int MIN_EXECUTE_TIMEOUT = 1000;// ms
	public static final int MAX_EXECUTE_TIMEOUT = 3600 * 1000;// ms
	
	public static final long MIN_EXECUTE_INTERVAL = 1000L;
	public static final long MAX_EXECUTE_INTERVAL = 31536000000000L;//10y 
}

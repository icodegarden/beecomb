package io.github.icodegarden.beecomb.client.util;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class WebUtils {
	private WebUtils() {
	}

	public static final int MAX_TOTAL_PAGES = 1000;

	public final static String AUTHORIZATION_HEADER = "Authorization";

	/**
	 * 是否内部服务间调用的标记
	 */
	public static final String HTTPHEADER_INTERNAL_RPC = "X-Internal-Rpc";
	/**
	 * 总页数
	 */
	public static final String HTTPHEADER_TOTALPAGES = "X-Total-Pages";
	/**
	 * 总条数
	 */
	public static final String HTTPHEADER_TOTALCOUNT = "X-Total-Count";
	/**
	 * 消息描述
	 */
	public static final String HTTPHEADER_MESSAGE = "X-Message";

}
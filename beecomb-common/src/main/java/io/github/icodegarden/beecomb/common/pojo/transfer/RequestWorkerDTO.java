package io.github.icodegarden.beecomb.common.pojo.transfer;

import java.io.Serializable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RequestWorkerDTO implements Serializable {
	private static final long serialVersionUID = 36827620108902360L;
	
	public static final String METHOD_RECEIVEJOB = "receiveJob";
	public static final String METHOD_IDQUEUED = "isQueued";
	public static final String METHOD_REMOVEJOB = "removeJob";
	public static final String METHOD_RUNJOB = "runJob";
	public static final String METHOD_QUEUEDSIZE = "queuedSize";
	public static final String METHOD_PING = "ping";

	private String method;
	private Object body;

	public RequestWorkerDTO() {
	}

	public RequestWorkerDTO(String method, Object body) {
		this.method = method;
		this.body = body;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "RequestWorkerDTO [method=" + method + ", body=" + body + "]";
	}

}

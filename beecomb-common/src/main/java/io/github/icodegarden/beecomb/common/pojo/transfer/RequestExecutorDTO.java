package io.github.icodegarden.beecomb.common.pojo.transfer;

import java.io.Serializable;

import io.github.icodegarden.nutrient.lang.BodyObject;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RequestExecutorDTO implements BodyObject<Object>, Serializable {
	private static final long serialVersionUID = 36827620108902360L;
	
	public static final String METHOD_RECEIVEJOB = "receiveJob";
	public static final String METHOD_ONPARALLELSUCCESS = "onParallelSuccess";
	public static final String METHOD_PING = "ping";

	private String method;
	private Object body;

	public RequestExecutorDTO() {
	}

	public RequestExecutorDTO(String method, Object body) {
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

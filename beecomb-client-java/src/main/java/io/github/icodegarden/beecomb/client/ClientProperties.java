package io.github.icodegarden.beecomb.client;

import io.github.icodegarden.beecomb.client.security.Authentication;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ClientProperties {

	private Authentication authentication;

	private Exchange exchange = new Exchange();

	public ClientProperties(Authentication authentication) {
		this.authentication = authentication;
	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public String toString() {
		return "ClientProperties [authentication=" + authentication + ", exchange=" + exchange + "]";
	}

	public static class Exchange {
		private int connectTimeout = -1;
		private int readTimeout = -1;

		public int getConnectTimeout() {
			return connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		public int getReadTimeout() {
			return readTimeout;
		}

		public void setReadTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
		}

		@Override
		public String toString() {
			return "Exchange [connectTimeout=" + connectTimeout + ", readTimeout=" + readTimeout + "]";
		}

	}
}
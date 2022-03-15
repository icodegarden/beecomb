package io.github.icodegarden.beecomb.client;

import java.util.List;

import io.github.icodegarden.beecomb.client.security.Authentication;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class UrlsClientProperties extends ClientProperties {

	private List<String> urls;

	public UrlsClientProperties(Authentication authentication, List<String> urls) {
		super(authentication);
		this.urls = urls;
	}

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	@Override
	public String toString() {
		return "UrlsClientProperties [urls=" + urls + ", toString()=" + super.toString() + "]";
	}
}
package io.github.icodegarden.beecomb.client;

import java.util.Arrays;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class UrlsBeeCombClientTests extends AbstractBeeCombClientTests {

	@Override
	protected BeeCombClient getBeeCombClient() {
		UrlsClientProperties clientProperties = new UrlsClientProperties(authentication,
				Arrays.asList("http://localhost:9898"));
		return new UrlsBeeCombClient(clientProperties);
	}

}

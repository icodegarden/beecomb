package io.github.icodegarden.beecomb.master.controller.openapi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
public class VersionOpenapiController {

	@GetMapping("openapi/v1/version")
	public ResponseEntity<String> getVersion(ServerWebExchange exchange) throws IOException {
		try (InputStream ins = ClassLoader.getSystemResourceAsStream("META-INF/MANIFEST.MF")) {
			Properties prop = new Properties();
			prop.load(ins);
			String appName = prop.getProperty("Implementation-Title");
			String appVersion = prop.getProperty("Implementation-Version");
			return ResponseEntity.ok(String.join("-", appName, appVersion));
		}
	}
}
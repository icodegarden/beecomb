package io.github.icodegarden.beecomb.master.controller.ruoyi;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Controller
public class SysLoginControllerRy {

	@GetMapping("/login")
	public String login(ServerWebExchange exchange, ConcurrentModel mmap) {
		// 是否开启记住我
		mmap.put("isRemembered", false);
		// 是否开启用户注册
		mmap.put("isAllowRegister", false);
		return "login";
	}
	
	@GetMapping("/logout")
	public String logout(ServerWebExchange exchange, ConcurrentModel mmap) {
		return "login";
	}

	@GetMapping("/unauth")
	public String unauth(ServerWebExchange exchange) {
		return "error/unauth";
	}

}

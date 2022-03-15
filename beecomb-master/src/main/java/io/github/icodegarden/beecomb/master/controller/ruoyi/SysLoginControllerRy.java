package io.github.icodegarden.beecomb.master.controller.ruoyi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Controller
public class SysLoginControllerRy {

	@GetMapping("/login")
	public String login(HttpServletRequest request, HttpServletResponse response, ModelMap mmap) {
		// 是否开启记住我
		mmap.put("isRemembered", false);
		// 是否开启用户注册
		mmap.put("isAllowRegister", false);
		return "login";
	}

	@GetMapping("/unauth")
	public String unauth() {
		return "error/unauth";
	}
}

package io.github.icodegarden.beecomb.master.controller.ruoyi;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

import io.github.icodegarden.beecomb.master.configuration.InstanceProperties;
import io.github.icodegarden.beecomb.master.configuration.InstanceProperties.Security.Jwt;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.ruoyi.ISysMenuService;
import io.github.icodegarden.beecomb.master.ruoyi.SysMenu;
import io.github.icodegarden.beecomb.master.ruoyi.SysUser;
import io.github.icodegarden.beecomb.master.security.JWTProperties;
import io.github.icodegarden.beecomb.master.security.JWTResolver;
import io.github.icodegarden.beecomb.master.security.UserDetails;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

/**
 * 首页 业务处理
 * 
 * @author ruoyi
 */
@Controller
public class SysIndexControllerRy 
{
	@Autowired
	private InstanceProperties instanceProperties;
    @Autowired
    private ISysMenuService menuService;

    // 系统首页
    @GetMapping("/index")
    public String index(HttpServletRequest request,ModelMap mmap)
    {
    	Jwt jwtConfig = instanceProperties.getSecurity().getJwt();
		JWTProperties jwtProperties = new JWTProperties(jwtConfig.getIssuer(), jwtConfig.getSecretKey(),
				jwtConfig.getTokenExpireSeconds());
    	
    	Authentication authentication = null;
		String jwt = getJwtFromCookie(request);
		if (StringUtils.hasText(jwt)) {
			try {
				JWTResolver jwtResolver = new JWTResolver(jwtProperties, jwt);
				authentication = jwtResolver.getAuthentication();
			} catch (TokenExpiredException e) {
			} catch (JWTVerificationException e) {
			}
		} 
    	
    	if(authentication == null) {
    		return "redirect:login";
    	}
    	
    	UserDetails userDetails = null;
		Object principal = authentication.getPrincipal();
		userDetails = (UserDetails) principal;
    	
        // 取身份信息
        UserPO user = userDetails.getUser();
        
        // 根据用户id取出菜单
        List<SysMenu> menus = menuService.selectMenusByUser(userDetails);
        mmap.put("menus", menus);
        mmap.put("user", of(user));
        mmap.put("sideTheme", "theme-dark");
        mmap.put("skinName", "skin-blue");
        Boolean footer = true;
        Boolean tagsView = true;
        mmap.put("footer", footer);
        mmap.put("tagsView", tagsView);
        mmap.put("mainClass", "");
        mmap.put("copyrightYear", 2021);
        mmap.put("demoEnabled", false);//是否显示demo页
        mmap.put("isDefaultModifyPwd", false);
        mmap.put("isPasswordExpired", false);
        mmap.put("isMobile", false);

//        // 菜单导航显示风格
//        String menuStyle = configService.selectConfigByKey("sys.index.menuStyle");
//        // 移动端，默认使左侧导航菜单，否则取默认配置
//        String indexStyle = ServletUtils.checkAgentIsMobile(ServletUtils.getRequest().getHeader("User-Agent")) ? "index" : menuStyle;
//
//        // 优先Cookie配置导航菜单
//        Cookie[] cookies = ServletUtils.getRequest().getCookies();
//        for (Cookie cookie : cookies)
//        {
//            if (StringUtils.isNotEmpty(cookie.getName()) && "nav-style".equalsIgnoreCase(cookie.getName()))
//            {
//                indexStyle = cookie.getValue();
//                break;
//            }
//        }
//        String webIndex = "topnav".equalsIgnoreCase(indexStyle) ? "index-topnav" : "index";
        return "index";
    }
    
    private String getJwtFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (WebUtils.AUTHORIZATION_HEADER.equals(cookie.getName())) {
				String value = cookie.getValue();
				return WebUtils.resolveBearerToken(value, "_");
			}
		}
		return null;
	}
    
    private SysUser of(UserPO user) {
    	SysUser sysUser = new SysUser();
        sysUser.setAvatar(null);
        sysUser.setEmail(user.getEmail());
        sysUser.setLoginName(user.getUsername());
        sysUser.setPhonenumber(user.getPhone());
        sysUser.setUserId(user.getId());
        sysUser.setUserName(user.getName());
//        sysUser.setUserType(userType);
        return sysUser;
    }
    

//    // 锁定屏幕
//    @GetMapping("/lockscreen")
//    public String lockscreen(ModelMap mmap)
//    {
//        mmap.put("user", getSysUser());
//        ServletUtils.getSession().setAttribute(ShiroConstants.LOCK_SCREEN, true);
//        return "lock";
//    }
//
//    // 解锁屏幕
//    @PostMapping("/unlockscreen")
//    @ResponseBody
//    public AjaxResult unlockscreen(String password)
//    {
//        SysUser user = getSysUser();
//        if (StringUtils.isNull(user))
//        {
//            return AjaxResult.error("服务器超时，请重新登录");
//        }
//        if (passwordService.matches(user, password))
//        {
//            ServletUtils.getSession().removeAttribute(ShiroConstants.LOCK_SCREEN);
//            return AjaxResult.success();
//        }
//        return AjaxResult.error("密码不正确，请重新输入。");
//    }

    // 切换主题
    @GetMapping("/system/switchSkin")
    public String switchSkin()
    {
        return "skin";
    }

//    // 切换菜单
//    @GetMapping("/system/menuStyle/{style}")
//    public void menuStyle(@PathVariable String style, HttpServletResponse response)
//    {
//        CookieUtils.setCookie(response, "nav-style", style);
//    }

    // 系统介绍
    @GetMapping("/system/main")
    public String main(ModelMap mmap)
    {
        mmap.put("version", "v1");
        return "main";
    }

//    // content-main class
//    public String contentMainClass(Boolean footer, Boolean tagsView)
//    {
//        if (!footer && !tagsView)
//        {
//            return "tagsview-footer-hide";
//        }
//        else if (!footer)
//        {
//            return "footer-hide";
//        }
//        else if (!tagsView)
//        {
//            return "tagsview-hide";
//        }
//        return StringUtils.EMPTY;
//    }
//
//    // 检查初始密码是否提醒修改
//    public boolean initPasswordIsModify(Date pwdUpdateDate)
//    {
//        Integer initPasswordModify = Convert.toInt(configService.selectConfigByKey("sys.account.initPasswordModify"));
//        return initPasswordModify != null && initPasswordModify == 1 && pwdUpdateDate == null;
//    }
//
//    // 检查密码是否过期
//    public boolean passwordIsExpiration(Date pwdUpdateDate)
//    {
//        Integer passwordValidateDays = Convert.toInt(configService.selectConfigByKey("sys.account.passwordValidateDays"));
//        if (passwordValidateDays != null && passwordValidateDays > 0)
//        {
//            if (StringUtils.isNull(pwdUpdateDate))
//            {
//                // 如果从未修改过初始密码，直接提醒过期
//                return true;
//            }
//            Date nowDate = DateUtils.getNowDate();
//            return DateUtils.differentDaysByMillisecond(nowDate, pwdUpdateDate) > passwordValidateDays;
//        }
//        return false;
//    }
}

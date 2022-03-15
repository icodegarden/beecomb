package io.github.icodegarden.beecomb.master.ruoyi;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO;
import io.github.icodegarden.beecomb.master.pojo.persistence.UserPO.PlatformRole;
import io.github.icodegarden.beecomb.master.security.UserDetails;
import io.github.icodegarden.commons.lang.tuple.NullableTuple2;
import io.github.icodegarden.commons.lang.tuple.NullableTuples;

@Service
public class ISysMenuService {

	public List<SysMenu> selectMenusByUser(UserDetails userDetails) {
		UserPO user = userDetails.getUser();
		PlatformRole platformRole = user.getPlatformRole();
		if (PlatformRole.Admin == platformRole) {

		}

		List<SysMenu> result = new LinkedList<SysMenu>();

		addSysMenu(user, getJobLevel0(), result);

		addSysMenu(user, getClusterLevel0(), result);
		
		addSysMenu(user, getSysLevel0(), result);

		return result;
	}

	private void addSysMenu(UserPO user, NullableTuple2<PlatformRole, SysMenu> p, List<SysMenu> result) {
		if (p.getT1() == null || user.getPlatformRole() == p.getT1()) {
			result.add(p.getT2());
		}
	}

	private NullableTuple2<PlatformRole, SysMenu> getJobLevel0() {
		SysMenu m1 = getJobList();
		SysMenu m2 = getJobRecoveryRecordList();

		SysMenu sysMenu = new SysMenu();
		sysMenu.setMenuId(1L);
		sysMenu.setParentId(0L);
		sysMenu.setChildren(Arrays.asList(m1,m2));
		sysMenu.setMenuName("任务管理");
		sysMenu.setMenuType(null);
		sysMenu.setOrderNum("1");
		sysMenu.setParams(null);
		sysMenu.setUrl("#");
//		sysMenu.setVisible(visible);
		return NullableTuples.of(null, sysMenu);
	}

	private SysMenu getJobList() {
		SysMenu sysMenu = new SysMenu();
		sysMenu.setMenuId(100L);
		sysMenu.setParentId(1L);
		sysMenu.setChildren(null);
		sysMenu.setMenuName("任务列表");
		sysMenu.setMenuType(null);
		sysMenu.setOrderNum("1");
		sysMenu.setParams(null);
		sysMenu.setUrl("/view/job/list");
//		sysMenu.setVisible(visible);
		return sysMenu;
	}
	
	private SysMenu getJobRecoveryRecordList() {
		SysMenu sysMenu = new SysMenu();
		sysMenu.setMenuId(101L);
		sysMenu.setParentId(1L);
		sysMenu.setChildren(null);
		sysMenu.setMenuName("任务恢复记录列表");
		sysMenu.setMenuType(null);
		sysMenu.setOrderNum("2");
		sysMenu.setParams(null);
		sysMenu.setUrl("/view/jobRecoveryRecord/list");
//		sysMenu.setVisible(visible);
		return sysMenu;
	}
	
	private NullableTuple2<PlatformRole, SysMenu> getClusterLevel0() {
		SysMenu m1 = getNodeList();

		SysMenu sysMenu = new SysMenu();
		sysMenu.setMenuId(2L);//
		sysMenu.setParentId(0L);
		sysMenu.setChildren(Arrays.asList(m1));
		sysMenu.setMenuName("集群管理");
		sysMenu.setMenuType(null);
		sysMenu.setOrderNum("2");//
		sysMenu.setParams(null);
		sysMenu.setUrl("#");
//		sysMenu.setVisible(visible);
		return NullableTuples.of(null, sysMenu);
	}

	private SysMenu getNodeList() {
		SysMenu sysMenu = new SysMenu();
		sysMenu.setMenuId(200L);
		sysMenu.setParentId(2L);
		sysMenu.setChildren(null);
		sysMenu.setMenuName("Node列表");
		sysMenu.setMenuType(null);
		sysMenu.setOrderNum("1");
		sysMenu.setParams(null);
		sysMenu.setUrl("/view/node/list");
//		sysMenu.setVisible(visible);
		return sysMenu;
	}

	private NullableTuple2<PlatformRole, SysMenu> getSysLevel0() {
		SysMenu m1 = getUserList();

		SysMenu sysMenu = new SysMenu();
		sysMenu.setMenuId(3L);//
		sysMenu.setParentId(0L);
		sysMenu.setChildren(Arrays.asList(m1));
		sysMenu.setMenuName("系统管理");
		sysMenu.setMenuType(null);
		sysMenu.setOrderNum("3");//
		sysMenu.setParams(null);
		sysMenu.setUrl("#");
//		sysMenu.setVisible(visible);
		return NullableTuples.of(PlatformRole.Admin, sysMenu);
	}

	private SysMenu getUserList() {
		SysMenu sysMenu = new SysMenu();
		sysMenu.setMenuId(300L);
		sysMenu.setParentId(3L);
		sysMenu.setChildren(null);
		sysMenu.setMenuName("用户列表");
		sysMenu.setMenuType(null);
		sysMenu.setOrderNum("1");
		sysMenu.setParams(null);
		sysMenu.setUrl("/view/user/list");
//		sysMenu.setVisible(visible);
		return sysMenu;
	}
}

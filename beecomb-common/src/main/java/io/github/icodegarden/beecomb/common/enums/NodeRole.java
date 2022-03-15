package io.github.icodegarden.beecomb.common.enums;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public enum NodeRole {
	Master("master"), Worker("worker"), Executor("executor");

	private final String roleName;

	private NodeRole(String roleName) {
		this.roleName = roleName;
	}
	
	public String getRoleName() {
		return roleName;
	}
}
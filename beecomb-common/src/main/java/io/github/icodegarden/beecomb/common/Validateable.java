package io.github.icodegarden.beecomb.common;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Validateable {

	void validate() throws IllegalArgumentException;

	default boolean shouldUpdate() {
		return true;
	}
}

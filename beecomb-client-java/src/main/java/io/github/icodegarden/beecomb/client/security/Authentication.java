package io.github.icodegarden.beecomb.client.security;

import io.github.icodegarden.nutrient.lang.tuple.Tuple2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Authentication {

	/**
	 * 
	 * @return header name/value
	 * @throws AuthenticationException
	 */
	Tuple2<String/*header name*/, String/*value*/> httpToken() throws AuthenticationException;
	
}

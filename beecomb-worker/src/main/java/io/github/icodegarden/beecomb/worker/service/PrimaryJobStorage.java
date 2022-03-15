package io.github.icodegarden.beecomb.worker.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import io.github.icodegarden.commons.lang.result.Result1;
import io.github.icodegarden.commons.lang.result.Result2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Primary
@Service
public class PrimaryJobStorage extends BaseJobStorage {

	@Override
	public Result2<Boolean, RuntimeException> updateOnNoQualifiedExecutor(UpdateOnNoQualifiedExecutor update) {
		throw new RuntimeException("not supported");
	}

	@Override
	public Result1<RuntimeException> updateOnExecuteSuccess(UpdateOnExecuteSuccess update) {
		throw new RuntimeException("not supported");
	}

	@Override
	public Result2<Boolean, RuntimeException> updateOnExecuteFailed(UpdateOnExecuteFailed update) {
		throw new RuntimeException("not supported");
	}

}
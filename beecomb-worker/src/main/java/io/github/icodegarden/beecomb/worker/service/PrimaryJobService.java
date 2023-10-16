package io.github.icodegarden.beecomb.worker.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteFailedDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnExecuteSuccessDTO;
import io.github.icodegarden.beecomb.worker.pojo.transfer.UpdateOnNoQualifiedExecutorDTO;
import io.github.icodegarden.nutrient.lang.result.Result1;
import io.github.icodegarden.nutrient.lang.result.Result2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Primary
@Service
public class PrimaryJobService extends BaseJobService {

	@Override
	public Result2<Boolean, RuntimeException> updateOnNoQualifiedExecutor(UpdateOnNoQualifiedExecutorDTO update) {
		throw new RuntimeException("not supported");
	}

	@Override
	public Result1<RuntimeException> updateOnExecuteSuccess(UpdateOnExecuteSuccessDTO update) {
		throw new RuntimeException("not supported");
	}

	@Override
	public Result2<Boolean, RuntimeException> updateOnExecuteFailed(UpdateOnExecuteFailedDTO update) {
		throw new RuntimeException("not supported");
	}

}
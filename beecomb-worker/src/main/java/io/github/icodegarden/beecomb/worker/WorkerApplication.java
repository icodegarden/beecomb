package io.github.icodegarden.beecomb.worker;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.esotericsoftware.kryo.serializers.FieldSerializer;

import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.commons.exchange.DefaultInstanceExchangeResult;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication(scanBasePackages = { "io.github.icodegarden.beecomb.worker",
		"io.github.icodegarden.beecomb.common.backend" })
public class WorkerApplication {

	public static void main(String[] args) throws IOException, InterruptedException {
		/**
		 * TODO remove
		 */
//		KryoSerializer.AbstractKryoFactory.configKryoFactoryCustom(kryo -> {
//			FieldSerializer<?> fieldSerializer = new FieldSerializer<>(kryo, ExecuteJobResult.class);
//			fieldSerializer.removeField("onParallelSuccessCallback");
//			kryo.register(ExecuteJobResult.class, fieldSerializer);
//		});

		SpringApplication.run(WorkerApplication.class, args);
	}
}
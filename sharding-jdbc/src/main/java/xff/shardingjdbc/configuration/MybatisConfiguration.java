package xff.shardingjdbc.configuration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
@MapperScan(basePackages = "xff.shardingjdbc.mapper")
public class MybatisConfiguration {

}

package xff.shardingjdbc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import xff.shardingjdbc.service.CRUDService;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication
public class ShardingjdbcApplication {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext run = SpringApplication.run(ShardingjdbcApplication.class, args);
        CRUDService CRUDService = run.getBean(CRUDService.class);
        for(;;) {
        
        	CRUDService.create();
        	Thread.sleep(100);
        }
        
    }
    
    @Bean
    public DataSource dataSource() throws SQLException, IOException {
    	URL url = YamlShardingSphereDataSourceFactory.class.getClassLoader().getResource("shardingsphere-jdbc.yml");
    	String file = url.getFile();
    	System.out.println(file);
    	DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(file));
    	return dataSource;
    }
}

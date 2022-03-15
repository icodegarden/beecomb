package xff.shardingjdbc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NoSpringApplication {

//    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
////    	URL url = YamlShardingDataSourceFactory.class.getClassLoader().getResource("sharding-jdbc.yml");
//    	URL url = YamlShardingSphereDataSourceFactory.class.getClassLoader().getResource("shardingsphere-jdbc.yml");
//    	String file = url.getFile();
//    	System.out.println(file);
////    	DataSource dataSource = YamlShardingDataSourceFactory.createDataSource(new File(file));
//    	DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(file));
//    	System.out.println(dataSource);
//    	
//    	for(;;) {
////    		String insert = "insert into job_main(`name`,`type`,`executor_name`,`job_handler_name`)  values (?,?,?,?)";
//    		String insert = "insert into job_main(name,type,executor_name,job_handler_name) values('job','Delay','aaa','bb');";
//        	try (
//        	        Connection conn = dataSource.getConnection();
//        	        PreparedStatement preparedStatement = conn.prepareStatement(insert)) {
//        		
////        	    preparedStatement.setString(1, "job");
////        	    preparedStatement.setString(2, "Delay");
////        	    preparedStatement.setString(3, "ddd");
////        	    preparedStatement.setString(4, "bbb");
//        	    boolean result = preparedStatement.execute();
//       	    	System.out.println(result);
//        	}
//        	Thread.sleep(100);
//    	}
//    	
//    	
////    	String sql = "SELECT * FROM t_order o LEFT JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=?";
////    	try (
////    	        Connection conn = dataSource.getConnection();
////    	        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
////    	    preparedStatement.setLong(1, 1001);
////    	    try (ResultSet rs = preparedStatement.executeQuery()) {
////    	        while(rs.next()) {
////    	            System.out.println(rs.getLong(1));
////    	            System.out.println(rs.getLong(2));
////    	        }
////    	    }
////    	}
//    }
    
    
    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
    	URL url = YamlShardingSphereDataSourceFactory.class.getClassLoader().getResource("shardingsphere-jdbc.yml");
    	String file = url.getFile();
    	System.out.println(file);
    	DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(file));
    	System.out.println(dataSource);
    	
    	for(;;) {
    		String insert = "insert into t_order(user_id,amount) values(?,?)";
        	try (
        	        Connection conn = dataSource.getConnection();
        	        PreparedStatement preparedStatement = conn.prepareStatement(insert)) {
        	    preparedStatement.setInt(1, 1001);
        	    preparedStatement.setInt(2, 500);
        	    boolean result = preparedStatement.execute();
       	    	System.out.println(result);
        	}
        	
        	Thread.sleep(100);
    	}
    }
}

package xff.shardingjdbc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import xff.shardingjdbc.mapper.ConfigMapper;
import xff.shardingjdbc.mapper.OrderMapper;
import xff.shardingjdbc.po.OrderPo;

@Service
public class CRUDService {
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	ConfigMapper configMapper;
	
	@Transactional
	public void create() {
		long[] userIds = {1000,1001};
		
		OrderPo orderPo = new OrderPo();
        orderPo.setUserId(1001L);
        orderPo.setAmount(500);
        orderMapper.add(orderPo);
        System.out.println(orderPo);
        
//        SnowflakeShardingKeyGenerator snowflakeShardingKeyGenerator = new SnowflakeShardingKeyGenerator();
//        Comparable<?> generateKey = snowflakeShardingKeyGenerator.generateKey();
//        Long id = (Long)generateKey;
//        
//        ConfigPo configPo = new ConfigPo();
//        configPo.setId(id);
//        configPo.setData(System.currentTimeMillis()+"");
//        configMapper.add(configPo);
//        System.out.println(configPo);
        
	}
}

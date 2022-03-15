package xff.shardingjdbc.mapper;

import xff.shardingjdbc.po.OrderPo;
/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface OrderMapper {

	void add(OrderPo po);
	
	OrderPo findOne(long orderId);
	
}

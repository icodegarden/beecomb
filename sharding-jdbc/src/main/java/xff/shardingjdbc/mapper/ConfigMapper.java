package xff.shardingjdbc.mapper;

import xff.shardingjdbc.po.ConfigPo;
/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ConfigMapper {

	void add(ConfigPo po);
	
	void update(long id);
}

package xff.shardingjdbc.po;
/**
 * 
 * @author Fangfang.Xu
 *
 */

public class ConfigPo {

	Long id;
	String data;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "ConfigPo [id=" + id + ", data=" + data + "]";
	}
	
	
}

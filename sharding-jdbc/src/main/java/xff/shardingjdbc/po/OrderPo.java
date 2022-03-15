package xff.shardingjdbc.po;
/**
 * 
 * @author Fangfang.Xu
 *
 */

public class OrderPo implements Comparable{

	Long orderId;
	Long userId;
	Integer amount;
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	@Override
	public String toString() {
		return "OrderPo [orderId=" + orderId + ", userId=" + userId + ", amount=" + amount + "]";
	}
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}

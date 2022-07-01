package io.github.icodegarden.beecomb.common.executor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ShardObject {

	int getShard();

	void setShard(int shard);

	int getShardTotal();

	void setShardTotal(int shardTotal);

}
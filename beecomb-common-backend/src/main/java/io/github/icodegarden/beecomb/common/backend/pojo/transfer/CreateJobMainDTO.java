package io.github.icodegarden.beecomb.common.backend.pojo.transfer;

import io.github.icodegarden.beecomb.common.Validateable;
import io.github.icodegarden.beecomb.common.enums.JobType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Setter
@Getter
@ToString
public class CreateJobMainDTO implements Validateable {

	/**
	 * 可以给也可以不给，不给自动生成并赋予这个值
	 */
	private Long id;

	private String uuid;// varchar(64) UNIQUE comment '用户可以指定,默认null',
	private String name;// varchar(30) NOT NULL,
	private JobType type;// tinyint NOT NULL comment '任务类型 0延时 1调度',
	private String executorName;// varchar(30) NOT NULL,
	private String jobHandlerName;// varchar(30) NOT NULL,
	private Integer priority;// tinyint NOT NULL default 5 comment '1-10仅当资源不足时起作用',
	private Integer weight;// tinyint NOT NULL default 1 comment '任务重量等级',
	private Boolean parallel;
	private Integer maxParallelShards;
	private Integer executeTimeout;// int NOT NULL default 10000 comment 'ms',

	/**
	 * 因createdBy只有具有security的才给的出，因此作为入参
	 */
	private String createdBy;

	@Override
	public void validate() throws IllegalArgumentException {
	}
}

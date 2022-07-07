package io.github.icodegarden.beecomb.master.pojo.persistence;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class TableDataCountPO {

	private Long id;
	private String tableName;
	private Long dataCount;
	private LocalDateTime updatedAt;// timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

}

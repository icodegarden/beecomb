package io.github.icodegarden.beecomb.master.pojo.persistence;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class ReportLinePO {

	private Long id;// bigint unsigned NOT NULL AUTO_INCREMENT,
	private Type type;
	private String content;
	private LocalDateTime updatedAt;

	@Setter
	@Getter
	public static class Update {
		private Long id;// bigint unsigned NOT NULL AUTO_INCREMENT,

		private String content;
		private LocalDateTime updatedAt;// timestamp NOT NULL COMMENT '最后修改时间',
	}

	public static enum Type {
		JobTotalDayN, //
		JobQueuedDayN, //
		JobNoQueuedNoEndDayN, //
		JobEndLastExecuteSuccessDayN, //
		JobExecuteRecordTotalDayN
	}

	/**
	 * 
	 * @param contentSrc 已存在的content
	 */
	public static String contentOfJsonArray(@Nullable String contentSrc, Object obj, int maxSize) {
		if (!StringUtils.hasText(contentSrc)) {
			contentSrc = "[]";
		}
		String today = SystemUtils.STANDARD_DATE_FORMATTER.format(SystemUtils.now());

		/**
		 * 去掉今天的
		 */
		List<Map> contentList = JsonUtils.deserializeArray(contentSrc, Map.class);
		contentList = contentList.stream().filter(map -> {
			return !map.get("dt").equals(today);
		}).collect(Collectors.toList());

		/**
		 * 加上今天的
		 */
		Map<String, Object> map = new HashMap<String, Object>(2, 1);
		map.put("dt", today);
		map.put("data", obj);
		contentList.add(map);
		/**
		 * 只保留maxSize
		 */
		if (contentList.size() > maxSize) {
			contentList = contentList.subList(contentList.size() - maxSize, contentList.size());
		}

		return JsonUtils.serialize(contentList);
	}
}

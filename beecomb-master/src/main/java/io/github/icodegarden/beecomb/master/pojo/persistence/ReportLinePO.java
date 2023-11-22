package io.github.icodegarden.beecomb.master.pojo.persistence;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import io.github.icodegarden.nutrient.lang.annotation.Nullable;
import io.github.icodegarden.nutrient.lang.util.JsonUtils;
import io.github.icodegarden.nutrient.lang.util.SystemUtils;
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
		JobEndLastExecuteFailedDayN, //
		JobExecuteRecordTotalDayN
	}

	/**
	 * 
	 * @param contentSrc 已存在的content
	 */
	public static String contentOfJsonArray(boolean dayIncr, @Nullable String contentSrc, List obj, int maxSize) {
		if (!StringUtils.hasText(contentSrc)) {
			contentSrc = "[]";
		}

		if (dayIncr) {
			/**
			 * 日增模式
			 */
			String latestday = SystemUtils.STANDARD_DATE_FORMATTER.format(SystemUtils.now().minusDays(1));// 今天统计的增量是昨天整天的部分
			String preday = SystemUtils.STANDARD_DATE_FORMATTER.format(SystemUtils.now().minusDays(2));// 再往前一天

			List<Map> contentList = JsonUtils.deserializeArray(contentSrc, Map.class);
			/*
			 * 找出前一天的(如果前一天数据不存在则往前找)
			 */
			Map predayDataMap = contentList.get(contentList.size() - 1);
			/*
			 * 把前一天的量累加到最新一天的
			 */
			if (predayDataMap != null) {
				List<Map<String, Object>> data = (List) predayDataMap.get("data");// 前一天的

				String jsonarr = JsonUtils.serialize(obj);
				List<Map<String, Object>> list = (List) JsonUtils.deserializeArray(jsonarr, Map.class);// 最新天的

				LinkedList<Map<String, Object>> resultList = new LinkedList<Map<String, Object>>();

				for (Map<String, Object> l : list) {
					List<String> noCountKey_keys = l.keySet().stream().filter(k -> !k.equals("count"))
							.collect(Collectors.toList());
					for (Map<String, Object> d : data) {
						boolean allValuesMatch = noCountKey_keys.stream().allMatch(k -> {
							return l.get(k) != null && l.get(k).equals(d.get(k));
						});
						if (allValuesMatch) {
							// 能够匹配的上所有withoutCountKeys的数据，说明是能对应的部分，需要累加
							long src = Long.parseLong(l.get("count").toString());
							long plus = Long.parseLong(d.get("count").toString());
							l.put("count", src + plus);

							d.put("merged", true);// 标记
						}
					}

					resultList.add(l);
				}
				// 需要反向来一遍
				for (Map<String, Object> d : data) {
					if (d.get("merged") != null) {
						continue;
					}

					resultList.add(d);// 没有merged的肯定是差异部分
				}

				/*
				 * 去掉最新的
				 */
				contentList = contentList.stream().filter(map -> {
					return !map.get("dt").equals(latestday);
				}).collect(Collectors.toList());
				/*
				 * 加上最新的
				 */
				Map<String, Object> map = new HashMap<String, Object>(2, 1);
				map.put("dt", latestday);
				map.put("data", resultList);
				contentList.add(map);
			} else {
				/*
				 * 去掉最新的
				 */
				contentList = contentList.stream().filter(map -> {
					return !map.get("dt").equals(latestday);
				}).collect(Collectors.toList());
				/*
				 * 加上最新的
				 */
				Map<String, Object> map = new HashMap<String, Object>(2, 1);
				map.put("dt", latestday);
				map.put("data", obj);
				contentList.add(map);
			}

			/*
			 * 只保留maxSize
			 */
			if (contentList.size() > maxSize) {
				contentList = contentList.subList(contentList.size() - maxSize, contentList.size());
			}

			return JsonUtils.serialize(contentList);
		} else {
			/**
			 * 全量模式
			 */

			String today = SystemUtils.STANDARD_DATE_FORMATTER.format(SystemUtils.now());
			/*
			 * 去掉今天的
			 */
			List<Map> contentList = JsonUtils.deserializeArray(contentSrc, Map.class);
			contentList = contentList.stream().filter(map -> {
				return !map.get("dt").equals(today);
			}).collect(Collectors.toList());

			/*
			 * 加上今天的
			 */
			Map<String, Object> map = new HashMap<String, Object>(2, 1);
			map.put("dt", today);
			map.put("data", obj);
			contentList.add(map);

			/*
			 * 只保留maxSize
			 */
			if (contentList.size() > maxSize) {
				contentList = contentList.subList(contentList.size() - maxSize, contentList.size());
			}

			return JsonUtils.serialize(contentList);
		}
	}
}

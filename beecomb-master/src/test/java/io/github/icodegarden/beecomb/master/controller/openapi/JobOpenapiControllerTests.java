package io.github.icodegarden.beecomb.master.controller.openapi;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.beecomb.common.backend.manager.JobMainManager;
import io.github.icodegarden.beecomb.common.backend.pojo.query.JobMainQuery;
import io.github.icodegarden.beecomb.common.backend.pojo.view.JobMainVO;
import io.github.icodegarden.beecomb.common.enums.JobType;
import io.github.icodegarden.beecomb.master.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.CreateJobOpenapiDTO;
import io.github.icodegarden.beecomb.master.pojo.transfer.openapi.UpdateJobOpenapiDTO;
import io.github.icodegarden.nutrient.lang.util.JsonUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@WebMvcTest(MallOpenapiController.class)

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
public class JobOpenapiControllerTests {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private JobMainManager jobMainManager;

	String token;

	@BeforeEach
	void init() throws UnsupportedEncodingException {
		String str = "beecomb:beecomb";
		byte[] encode = Base64.getEncoder().encode(str.getBytes("utf-8"));
		token = "Basic " + new String(encode, "utf-8");
	}

	@Test
	public void createJob() throws Exception {
		CreateJobOpenapiDTO body = new CreateJobOpenapiDTO();
		body.setUuid(UUID.randomUUID().toString());
		body.setName("myjob1");
		body.setType(JobType.Delay);
		body.setExecutorName("e1");
		body.setJobHandlerName("h1");
		body.setParams("param");
		body.setDesc("desc");
		CreateJobDTO.Delay delay = new CreateJobDTO.Delay();
		delay.setDelay(3000L);
		body.setDelay(delay);

		mvc.perform(post("/openapi/v1/jobs")
//               .with(user("blaze").password("Q1w2e3r4"))
				.header("Authorization", token).contentType(APPLICATION_JSON).content(JsonUtils.serialize(body)))
				.andExpect(status().isOk()).andExpect(jsonPath("job").isNotEmpty())
				.andExpect(content().string(new BaseMatcher<String>() {
					@Override
					public boolean matches(Object item) {
						Test_CreateJobOpenapiVO vo = JsonUtils.deserialize(item.toString(),
								Test_CreateJobOpenapiVO.class);
						return vo.getJob() != null && vo.getJob().getName() != null;
					}

					@Override
					public void describeTo(Description description) {
					}
				}));

	}

	@Test
	public void pageJobs() throws Exception {
		createJob();

		mvc.perform(get("/openapi/v1/jobs")
//               .with(user("blaze").password("Q1w2e3r4"))
				.header("Authorization", token).contentType(APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().string(new BaseMatcher<String>() {
					@Override
					public boolean matches(Object item) {
						List<Test_PageJobsOpenapiVO> vos = JsonUtils.deserializeArray(item.toString(),
								Test_PageJobsOpenapiVO.class);
						return vos.size() != 0;
					}

					@Override
					public void describeTo(Description description) {
					}
				}));

	}

	@Test
	public void getJob() throws Exception {
		createJob();
		Long id = jobMainManager.page(JobMainQuery.builder().build()).get(0).getId();

		mvc.perform(get("/openapi/v1/jobs/" + id)
//               .with(user("blaze").password("Q1w2e3r4"))
				.header("Authorization", token).contentType(APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().string(new BaseMatcher<String>() {
					@Override
					public boolean matches(Object item) {
						Test_GetJobOpenapiVO vo = JsonUtils.deserialize(item.toString(), Test_GetJobOpenapiVO.class);
						return vo.getName() != null;
					}

					@Override
					public void describeTo(Description description) {
					}
				}));

	}
	
	@Test
	public void getJobByUUID() throws Exception {
		createJob();
		String uuid = jobMainManager.page(JobMainQuery.builder().build()).get(0).getUuid();

		mvc.perform(get("/openapi/v1/jobs/uuid/" + uuid)
//               .with(user("blaze").password("Q1w2e3r4"))
				.header("Authorization", token).contentType(APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().string(new BaseMatcher<String>() {
					@Override
					public boolean matches(Object item) {
						Test_GetJobOpenapiVO vo = JsonUtils.deserialize(item.toString(), Test_GetJobOpenapiVO.class);
						return vo.getName() != null;
					}

					@Override
					public void describeTo(Description description) {
					}
				}));

	}
	
	@Test
	public void updateJob() throws Exception {
		createJob();
		Long id = jobMainManager.page(JobMainQuery.builder().build()).get(0).getId();
		
		UpdateJobOpenapiDTO body = new UpdateJobOpenapiDTO();
		body.setId(id);
		body.setName("newname");
		body.setParams("newparam");
		body.setDesc("newdesc");
		
		mvc.perform(put("/openapi/v1/jobs")
//               .with(user("blaze").password("Q1w2e3r4"))
				.header("Authorization", token).contentType(APPLICATION_JSON).content(JsonUtils.serialize(body)))
				.andExpect(status().isOk());

		JobMainVO one = jobMainManager.findOne(id, JobMainQuery.With.WITH_MOST);
		Assertions.assertThat(one.getName()).isEqualTo(body.getName());
		Assertions.assertThat(one.getJobDetail().getParams()).isEqualTo(body.getParams());
		Assertions.assertThat(one.getJobDetail().getDesc()).isEqualTo(body.getDesc());
	}

	@Data
	public static class Test_CreateJobOpenapiVO {
		private Job job;
		private String dispatchException;

		@Data
		public static class Job {
			private Long id;
			private String uuid;
			private String name;
			private JobType type;
			private Integer priority;
			private Integer weight;
			private Boolean queued;
			private String queuedAtInstance;

		}
	}

	@Setter
	@Getter
	@ToString
	public static class Test_GetJobOpenapiVO {

		/**
		 * main
		 */
		private Long id;// bigint NOT NULL AUTO_INCREMENT,
		private String uuid;// varchar(64) UNIQUE comment '用户可以指定,默认null',
		private String name;// varchar(30) NOT NULL,
		private JobType type;// tinyint NOT NULL comment '任务类型 0延时 1调度',

		private String executorName;// varchar(30) NOT NULL,

		private String jobHandlerName;// varchar(30) NOT NULL,

		private Integer priority;// tinyint NOT NULL default 3 comment '仅当资源不足时起作用',

		private Integer weight;// tinyint NOT NULL default 1 comment '任务重量等级',

		private Boolean parallel;
		private Integer maxParallelShards;

		private Boolean queued;// bit NOT NULL default 0,
		private LocalDateTime queuedAt;// timestamp,
		private String queuedAtInstance;// varchar(21) comment 'ip:port',
		private LocalDateTime lastTrigAt;// timestamp,
		private String lastTrigResult;// varchar(200),
		private String lastExecuteExecutor;// varchar(21) comment 'ip:port',
		private String lastExecuteReturns;// varchar(200),
		private Boolean lastExecuteSuccess;// bit NOT NULL default 0,

		private Integer executeTimeout;// int comment 'ms',
		private LocalDateTime nextTrigAt;// timestamp comment '下次触发时间,初始是null',

		private Boolean end;// bit NOT NULL default 0 comment '是否已结束',
		private String createdBy;// varchar(30) comment '租户名',
		private LocalDateTime createdAt;// timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

		/**
		 * detail
		 */
		private String params;// TEXT comment '任务参数',
		private String desc;// varchar(200) comment '任务描述',

		private Delay delay;
		private Schedule schedule;

		@Setter
		@Getter
		@ToString
		public static class Delay {
			private Long delay;// int comment 'ms',
			private Integer retryOnExecuteFailed;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败重试次数，包括连接失败、超时等',
			private Integer retryBackoffOnExecuteFailed;// int NOT NULL DEFAULT 1000 comment 'ms要求 gte 1000',
			private Integer retriedTimesOnExecuteFailed;// smallint NOT NULL DEFAULT 0 comment 'executor执行失败已重试次数',
			private Integer retryOnNoQualified;// smallint NOT NULL DEFAULT 0 comment '没有可用的executor时重试次数，包括不在线、超载时',
			private Integer retryBackoffOnNoQualified;// int NOT NULL DEFAULT 30000 comment 'ms要求 gte 5000',
			private Integer retriedTimesOnNoQualified;// smallint NOT NULL DEFAULT 0 comment '没有可用的executor时已重试次数',
		}

		@Setter
		@Getter
		@ToString
		public static class Schedule {
			private Long scheduleFixRate;
			private Long scheduleFixDelay;
			private String sheduleCron;// varchar(20),
			private Long scheduledTimes;// bigint,
		}
	}
	
	@Data
	public static class Test_PageJobsOpenapiVO extends Test_GetJobOpenapiVO {
	}
}
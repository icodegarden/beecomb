package io.github.icodegarden.beecomb.client;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import io.github.icodegarden.beecomb.client.pojo.query.JobQuery;
import io.github.icodegarden.beecomb.client.pojo.query.JobQuery.JobWith;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateJobDTO;
import io.github.icodegarden.beecomb.client.pojo.transfer.UpdateJobDTO;
import io.github.icodegarden.beecomb.client.pojo.view.CreateJobVO;
import io.github.icodegarden.beecomb.client.pojo.view.JobVO;
import io.github.icodegarden.beecomb.client.pojo.view.PageVO;
import io.github.icodegarden.beecomb.client.util.WebUtils;
import io.github.icodegarden.commons.exchange.Exchanger;
import io.github.icodegarden.commons.exchange.Protocol;
import io.github.icodegarden.commons.exchange.ShardExchangeResult;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.http.HttpEntity;
import io.github.icodegarden.commons.exchange.http.HttpHeaders;
import io.github.icodegarden.commons.exchange.http.HttpMethod;
import io.github.icodegarden.commons.exchange.http.SimpleRestHttpProtocol;
import io.github.icodegarden.commons.lang.tuple.Tuple2;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AbstractBeeCombClient implements BeeCombClient {

	protected static final List<String> EXTRACT_HEADERS = Arrays.asList(WebUtils.HTTPHEADER_MESSAGE);

	protected final ClientProperties clientProperties;

	public AbstractBeeCombClient(ClientProperties clientProperties) {
		this.clientProperties = clientProperties;
	}

	protected Protocol buildProtocol(String path, HttpMethod method, Object responseType) {
		Tuple2<String, String> httpToken = clientProperties.getAuthentication().httpToken();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set(httpToken.getT1(), httpToken.getT2());

		SimpleRestHttpProtocol httpProtocol = null;
		if (responseType instanceof Class) {
			httpProtocol = new SimpleRestHttpProtocol(path, method, httpHeaders, (Class) responseType);
		} else if (responseType instanceof ParameterizedTypeReference) {
			httpProtocol = new SimpleRestHttpProtocol(path, method, httpHeaders,
					(ParameterizedTypeReference) responseType);
		} else {
			throw new IllegalArgumentException("responseType must be Class or ParameterizedTypeReference");
		}
		httpProtocol.setConnectTimeout(clientProperties.getExchange().getConnectTimeout());
		httpProtocol.setReadTimeout(clientProperties.getExchange().getReadTimeout());
		httpProtocol.setExtractHeadersOn4xx(EXTRACT_HEADERS);
		httpProtocol.setExtractHeadersOn5xx(EXTRACT_HEADERS);
		return httpProtocol;
	}

	protected abstract String pathPrefix();

	protected abstract Exchanger<ShardExchangeResult> buildExchanger(Protocol protocol);

	@Override
	public CreateJobVO createJob(CreateJobDTO job) throws ExchangeException {
		return doCreateJob(false, job);
	}

	@Override
	public CreateJobVO createJobAsync(CreateJobDTO job) throws ExchangeException {
		return doCreateJob(true, job);
	}

	private CreateJobVO doCreateJob(boolean async, CreateJobDTO job) throws ExchangeException {
		Protocol protocol = buildProtocol(pathPrefix() + "/openapi/v1/jobs?async=" + async, HttpMethod.POST,
				CreateJobVO.class);

		Exchanger<ShardExchangeResult> exchanger = buildExchanger(protocol);

		ShardExchangeResult shardExchangeResult = exchanger.exchange(job, Integer.MAX_VALUE);

		HttpEntity<CreateJobVO> httpEntity = (HttpEntity) shardExchangeResult.response();
		return httpEntity.getBody();
	}

	@Override
	public PageVO<JobVO> pageJobs(JobQuery query) throws ExchangeException {
		StringBuilder sb = new StringBuilder(64);
		sb.append(pathPrefix()).append("/openapi/v1/jobs?page=").append(query.getPage())
				.append("&size=" + query.getSize());
		if (query.getEnd() != null) {
			sb.append("&end=").append(query.getEnd());
		}
//		if (query.getLastExecuteSuccess() != null) {
//			sb.append("&lastExecuteSuccess=").append(query.getLastExecuteSuccess());
//		}
//		if (query.getParallel() != null) {
//			sb.append("&parallel=").append(query.getParallel());
//		}
//		if (query.getQueued() != null) {
//			sb.append("&queued=").append(query.getQueued());
//		}
//		if (query.getCreatedAtGte() != null) {
//			sb.append("&createdAtGte=").append(JsonUtils.STANDARD_DATETIME_FORMATTER.format(query.getCreatedAtGte()));
//		}
//		if (query.getCreatedAtLte() != null) {
//			sb.append("&createdAtLte=").append(JsonUtils.STANDARD_DATETIME_FORMATTER.format(query.getCreatedAtLte()));
//		}
//		if (query.getLastTrigAtGte() != null) {
//			sb.append("&lastTrigAtGte=").append(JsonUtils.STANDARD_DATETIME_FORMATTER.format(query.getLastTrigAtGte()));
//		}
//		if (query.getLastTrigAtLte() != null) {
//			sb.append("&lastTrigAtLte=").append(JsonUtils.STANDARD_DATETIME_FORMATTER.format(query.getLastTrigAtLte()));
//		}
		if (query.getNameLike() != null) {
			sb.append("&nameLike=").append(query.getNameLike());
		}
		if (query.getType() != null) {
			sb.append("&type=").append(query.getType().name());
		}
		if (query.getUuid() != null) {
			sb.append("&uuid=").append(query.getUuid());
		}
		if (query.getExtParams() != null) {
			query.getExtParams().entrySet().forEach(entry -> {
				sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
			});
		}

		if (query.getWith() != null) {
			JobWith with = query.getWith();
			if (with.isWithCreatedAt()) {
				sb.append("&withCreatedAt=").append(with.isWithCreatedAt());
			}

			if (with.isWithCreatedBy()) {
				sb.append("&withCreatedBy=").append(with.isWithCreatedBy());
			}
			if (with.isWithDelay()) {
				sb.append("&withDelay=").append(with.isWithDelay());
			}
			if (with.isWithDesc()) {
				sb.append("&withDesc=").append(with.isWithDesc());
			}
			if (with.isWithLastExecuteExecutor()) {
				sb.append("&withLastExecuteExecutor=").append(with.isWithLastExecuteExecutor());
			}
			if (with.isWithLastExecuteReturns()) {
				sb.append("&withLastExecuteReturns=").append(with.isWithLastExecuteReturns());
			}
			if (with.isWithLastExecuteSuccess()) {
				sb.append("&withLastExecuteSuccess=").append(with.isWithLastExecuteSuccess());
			}
			if (with.isWithLastTrigResult()) {
				sb.append("&withLastTrigResult=").append(with.isWithLastTrigResult());
			}
			if (with.isWithParams()) {
				sb.append("&withParams=").append(with.isWithParams());
			}
			if (with.isWithQueuedAt()) {
				sb.append("&withQueuedAt=").append(with.isWithQueuedAt());
			}
			if (with.isWithQueuedAtInstance()) {
				sb.append("&withQueuedAtInstance=").append(with.isWithQueuedAtInstance());
			}
			if (with.isWithSchedule()) {
				sb.append("&withSchedule=").append(with.isWithSchedule());
			}
		}

		Protocol protocol = buildProtocol(sb.toString(), HttpMethod.GET,
				new ParameterizedTypeReference<List<JobVO>>() {
				});

		Exchanger<ShardExchangeResult> exchanger = buildExchanger(protocol);

		ShardExchangeResult shardExchangeResult = exchanger.exchange(null, Integer.MAX_VALUE);

		HttpEntity<List<JobVO>> httpEntity = (HttpEntity) shardExchangeResult.response();

		HttpHeaders headers = httpEntity.getHeaders();
		List<JobVO> list = httpEntity.getBody();

		String totalPageStr = headers.getFirst(WebUtils.HTTPHEADER_TOTALPAGES);
		String totalCountStr = headers.getFirst(WebUtils.HTTPHEADER_TOTALCOUNT);

		return new PageVO<JobVO>(query.getPage(), query.getSize(),
				totalPageStr != null ? Integer.parseInt(totalPageStr) : 0,
				totalCountStr != null ? Long.parseLong(totalCountStr) : 0, list);
	}

	@Override
	public JobVO getJob(Long jobId) throws ExchangeException {
		Protocol protocol = buildProtocol(pathPrefix() + "/openapi/v1/jobs/" + jobId, HttpMethod.GET,
				JobVO.class);

		Exchanger<ShardExchangeResult> exchanger = buildExchanger(protocol);

		ShardExchangeResult shardExchangeResult = exchanger.exchange(null, Integer.MAX_VALUE);

		HttpEntity<JobVO> httpEntity = (HttpEntity) shardExchangeResult.response();
		return httpEntity.getBody();
	}

	@Override
	public JobVO getJobByUUID(String uuid) throws ExchangeException {
		Protocol protocol = buildProtocol(pathPrefix() + "/openapi/v1/jobs/uuid/" + uuid, HttpMethod.GET,
				JobVO.class);

		Exchanger<ShardExchangeResult> exchanger = buildExchanger(protocol);

		ShardExchangeResult shardExchangeResult = exchanger.exchange(null, Integer.MAX_VALUE);

		HttpEntity<JobVO> httpEntity = (HttpEntity) shardExchangeResult.response();
		return httpEntity.getBody();
	}
	
	@Override
	public void updateJob(UpdateJobDTO update) throws ExchangeException {
		Protocol protocol = buildProtocol(pathPrefix() + "/openapi/v1/jobs", HttpMethod.PUT, String.class);

		Exchanger<ShardExchangeResult> exchanger = buildExchanger(protocol);

		exchanger.exchange(update, Integer.MAX_VALUE);
	}
}

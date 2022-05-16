package io.github.icodegarden.beecomb.client.pojo.request;

import java.time.LocalDateTime;
import java.util.Map;

import io.github.icodegarden.beecomb.common.enums.JobType;

/**
 *
 * @author Fangfang.Xu
 *
 */
public class JobQuery extends BaseQuery {

	private String uuid;
	private String nameLike;
	private JobType type;
	private Boolean parallel;
	private Boolean queued;
	private Boolean end;
//	private Boolean lastExecuteSuccess;
	private LocalDateTime createdAtGte;
	private LocalDateTime createdAtLte;
//	private LocalDateTime lastTrigAtGte;
//	private LocalDateTime lastTrigAtLte;

	/**
	 * 扩展查询参数
	 */
	private Map<String, String> extParams;

	private JobWith with;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Boolean getQueued() {
		return queued;
	}

	public void setQueued(Boolean queued) {
		this.queued = queued;
	}

	public Boolean getEnd() {
		return end;
	}

	public void setEnd(Boolean end) {
		this.end = end;
	}

	public String getNameLike() {
		return nameLike;
	}

	public void setNameLike(String nameLike) {
		this.nameLike = nameLike;
	}

	public JobType getType() {
		return type;
	}

	public void setType(JobType type) {
		this.type = type;
	}

	public Boolean getParallel() {
		return parallel;
	}

	public void setParallel(Boolean parallel) {
		this.parallel = parallel;
	}

//	public Boolean getLastExecuteSuccess() {
//		return lastExecuteSuccess;
//	}
//
//	public void setLastExecuteSuccess(Boolean lastExecuteSuccess) {
//		this.lastExecuteSuccess = lastExecuteSuccess;
//	}

	public LocalDateTime getCreatedAtGte() {
		return createdAtGte;
	}

	public void setCreatedAtGte(LocalDateTime createdAtGte) {
		this.createdAtGte = createdAtGte;
	}

	public LocalDateTime getCreatedAtLte() {
		return createdAtLte;
	}

	public void setCreatedAtLte(LocalDateTime createdAtLte) {
		this.createdAtLte = createdAtLte;
	}

//	public LocalDateTime getLastTrigAtGte() {
//		return lastTrigAtGte;
//	}
//
//	public void setLastTrigAtGte(LocalDateTime lastTrigAtGte) {
//		this.lastTrigAtGte = lastTrigAtGte;
//	}
//
//	public LocalDateTime getLastTrigAtLte() {
//		return lastTrigAtLte;
//	}
//
//	public void setLastTrigAtLte(LocalDateTime lastTrigAtLte) {
//		this.lastTrigAtLte = lastTrigAtLte;
//	}

	public Map<String, String> getExtParams() {
		return extParams;
	}

	public void setExtParams(Map<String, String> extParams) {
		this.extParams = extParams;
	}

	public JobWith getWith() {
		return with;
	}

	public void setWith(JobWith with) {
		this.with = with;
	}

	@Override
	public String toString() {
		return "JobQuery [uuid=" + uuid + ", nameLike=" + nameLike + ", type=" + type + ", parallel=" + parallel
				+ ", queued=" + queued + ", end=" + end + ", createdAtGte=" + createdAtGte + ", createdAtLte="
				+ createdAtLte + ", extParams=" + extParams + ", with=" + with + "]";
	}

	public static class JobWith {
		private boolean withQueuedAt;
		private boolean withQueuedAtInstance;
		private boolean withLastTrigResult;
		private boolean withLastExecuteExecutor;
		private boolean withLastExecuteReturns;
		private boolean withLastExecuteSuccess;
		private boolean withCreatedBy;
		private boolean withCreatedAt;

		private boolean withParams;
		private boolean withDesc;

		private boolean withDelay;
		private boolean withSchedule;

		public boolean isWithQueuedAt() {
			return withQueuedAt;
		}

		public void setWithQueuedAt(boolean withQueuedAt) {
			this.withQueuedAt = withQueuedAt;
		}

		public boolean isWithQueuedAtInstance() {
			return withQueuedAtInstance;
		}

		public void setWithQueuedAtInstance(boolean withQueuedAtInstance) {
			this.withQueuedAtInstance = withQueuedAtInstance;
		}

		public boolean isWithLastTrigResult() {
			return withLastTrigResult;
		}

		public void setWithLastTrigResult(boolean withLastTrigResult) {
			this.withLastTrigResult = withLastTrigResult;
		}

		public boolean isWithLastExecuteExecutor() {
			return withLastExecuteExecutor;
		}

		public void setWithLastExecuteExecutor(boolean withLastExecuteExecutor) {
			this.withLastExecuteExecutor = withLastExecuteExecutor;
		}

		public boolean isWithLastExecuteReturns() {
			return withLastExecuteReturns;
		}

		public void setWithLastExecuteReturns(boolean withLastExecuteReturns) {
			this.withLastExecuteReturns = withLastExecuteReturns;
		}

		public boolean isWithLastExecuteSuccess() {
			return withLastExecuteSuccess;
		}

		public void setWithLastExecuteSuccess(boolean withLastExecuteSuccess) {
			this.withLastExecuteSuccess = withLastExecuteSuccess;
		}

		public boolean isWithCreatedBy() {
			return withCreatedBy;
		}

		public void setWithCreatedBy(boolean withCreatedBy) {
			this.withCreatedBy = withCreatedBy;
		}

		public boolean isWithCreatedAt() {
			return withCreatedAt;
		}

		public void setWithCreatedAt(boolean withCreatedAt) {
			this.withCreatedAt = withCreatedAt;
		}

		public boolean isWithParams() {
			return withParams;
		}

		public void setWithParams(boolean withParams) {
			this.withParams = withParams;
		}

		public boolean isWithDesc() {
			return withDesc;
		}

		public void setWithDesc(boolean withDesc) {
			this.withDesc = withDesc;
		}

		public boolean isWithDelay() {
			return withDelay;
		}

		public void setWithDelay(boolean withDelay) {
			this.withDelay = withDelay;
		}

		public boolean isWithSchedule() {
			return withSchedule;
		}

		public void setWithSchedule(boolean withSchedule) {
			this.withSchedule = withSchedule;
		}

		@Override
		public String toString() {
			return "JobWith [withQueuedAt=" + withQueuedAt + ", withQueuedAtInstance=" + withQueuedAtInstance
					+ ", withLastTrigResult=" + withLastTrigResult + ", withLastExecuteExecutor="
					+ withLastExecuteExecutor + ", withLastExecuteReturns=" + withLastExecuteReturns
					+ ", withLastExecuteSuccess=" + withLastExecuteSuccess + ", withCreatedBy=" + withCreatedBy
					+ ", withCreatedAt=" + withCreatedAt + ", withParams=" + withParams + ", withDesc=" + withDesc
					+ ", withDelay=" + withDelay + ", withSchedule=" + withSchedule + "]";
		}

	}
}

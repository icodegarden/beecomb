-- drop DATABASE IF EXISTS `beecomb`;
-- create DATABASE `beecomb`;
-- USE beecomb;



DROP TABLE IF EXISTS `job_main`;
CREATE TABLE `job_main` (
  `id` bigint unsigned NOT NULL,
  `uuid` varchar(64) comment '用户可以指定,默认null',
  `name` varchar(30) NOT NULL,
  `type` enum('Delay','Schedule') NOT NULL,
  `executor_name` varchar(30) NOT NULL,
  `job_handler_name` varchar(30) NOT NULL,
  `priority` tinyint NOT NULL default 5 comment '1-10仅当任务恢复时起作用,越大越优先',
  `weight` tinyint NOT NULL default 1 comment '任务重量等级1-5',
  `is_parallel` bit NOT NULL default 0 comment '是否并行任务',
  `max_parallel_shards` smallint NOT NULL default 8 comment '最大并行数，2-64',
  `is_queued` bit NOT NULL default 0,
  `queued_at` timestamp,
  `queued_at_instance` varchar(21) comment 'ip:port,所在的worker实例',
  `last_trig_at` timestamp comment '任务调度触发时间',
  `last_execute_executor` varchar(21) comment 'ip:port',
  `is_last_execute_success` bit NOT NULL default 0,
  `execute_timeout` int NOT NULL default 10000 comment 'ms',
  `next_trig_at` timestamp comment '下次触发时间,初始是null',
  `is_end` bit NOT NULL default 0 comment '是否已结束',
  `created_by` varchar(30) comment 'user.username',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_uuid`(`uuid`), -- uuid不约束唯一，是否需要唯一由用户自己保障
  INDEX `idx_for_update_to_recovery`(`next_trig_at`),
  INDEX `idx_for_list_recovery`(`is_end`,`is_queued`,`priority`),  
  INDEX `idx_for_search`(`created_by`,`id`,`uuid`,`name`,`type`,`is_end`,`is_queued`,`is_parallel`,`is_last_execute_success`,`created_at`,`next_trig_at`),
  INDEX `idx_for_count_queued`(`is_queued`, `created_by`, `type`),
  INDEX `idx_for_count_end_success`(`is_end`, `is_last_execute_success`, `created_by`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `job_detail`;
CREATE TABLE `job_detail` (
  `job_id` bigint unsigned NOT NULL,
  `params` text comment '任务参数65535',
  `desc` varchar(200) comment '任务描述',
  `last_trig_result` text comment '触发结果,例如没有可选的executor实例64K',
  `last_execute_returns` text,
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `delay_job`;
CREATE TABLE `delay_job` (
  `job_id` bigint unsigned NOT NULL,
  `delay` int NOT NULL comment 'ms',
  `retry_on_execute_failed` smallint NOT NULL DEFAULT 0 comment 'executor执行失败重试次数，包括连接失败、超时、代码异常等',
  `retry_backoff_on_execute_failed` int NOT NULL DEFAULT 3000 comment 'ms要求 gte 1000',
  `retried_times_on_execute_failed` smallint NOT NULL DEFAULT 0 comment 'executor执行失败已重试次数',
  `retry_on_no_qualified` smallint NOT NULL DEFAULT 0 comment '没有合格的executor时重试次数，包括不在线、超载时',
  `retry_backoff_on_no_qualified` int NOT NULL DEFAULT 30000 comment 'ms要求 gte 5000',
  `retried_times_on_no_qualified` smallint NOT NULL DEFAULT 0 comment '没有合格的executor时已重试次数',
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `schedule_job`;
CREATE TABLE `schedule_job` (
  `job_id` bigint unsigned NOT NULL,
  `schedule_fix_rate` int comment 'ms',
  `schedule_fix_delay` int comment 'ms',
  `shedule_cron` varchar(20),
  `scheduled_times` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `job_execute_record`;
CREATE TABLE `job_execute_record` (
  `id` bigint unsigned NOT NULL,
  `job_id` bigint unsigned NOT NULL,
  `trig_at` timestamp NOT NULL comment '任务调度触发时间',
  `trig_result` text comment '触发结果,例如没有可选的executor实例64K',
  `execute_executor` varchar(21) comment 'ip:port',
  `execute_returns` varchar(200),
  `is_success` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idxs`(`job_id`,`is_success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `job_recovery_record`;
CREATE TABLE `job_recovery_record` (
  `job_id` bigint unsigned NOT NULL,
  `is_success` bit(1) NOT NULL,
  `desc` text comment '恢复结果描述65535',
  `recovery_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`job_id`),
  INDEX `idx_recovery_at`(`recovery_at`),
  INDEX `idx_is_success_recovery_at`(`is_success`,`recovery_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `report_line`;
CREATE TABLE `report_line` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(50) NOT NULL UNIQUE comment '报表类型',
  `content` JSON,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(24) NOT NULL UNIQUE COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '加密后的密码',
  `name` varchar(64) COMMENT '姓名',
  `email` varchar(128) COMMENT '邮箱地址',
  `phone` varchar(32),
  `is_actived` bit(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
  `platform_role` varchar(20) NOT NULL COMMENT '平台角色:管理员、普通用户、...',
  `created_by` varchar(32) NOT NULL COMMENT '创建人',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(32) NOT NULL COMMENT '最后修改人',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` ( `username`, `password`, `name`, `email`, `phone`, `is_actived`, `platform_role`, `created_by`, `created_at`, `updated_by`, `updated_at` )
VALUES( 'beecomb', '$2a$10$durkgWKpuCHeApmzQ2uyGuis8OsnyhzpRWLAR1k5Gemaa7PC9sy8m', 'beecomb', null, null, 1, 'Admin', 'sys', '2022-02-14 15:39:50', 'sys', '2022-02-14 15:39:50' );


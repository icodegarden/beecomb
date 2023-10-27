alter table `job_main` add column `label` varchar(30) DEFAULT NULL;
alter table `job_main` add index `idx_label`(`label`(20));
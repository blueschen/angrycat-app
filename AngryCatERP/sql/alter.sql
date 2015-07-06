-- 2015-07-06
ALTER TABLE `angrycat`.`shr_datachangelog` ADD COLUMN `action` VARCHAR(100) NULL COMMENT '紀錄行為' AFTER `note`;
-- 2015-07-20
ALTER TABLE `angrycat`.`member` ADD COLUMN `toVipEndDate` DATETIME NULL COMMENT 'VIP到期日' AFTER `vipDiscountUseDate`;
ALTER TABLE `angrycat`.`member` CHANGE `postalCode` `postalCode` VARCHAR(100) CHARSET utf8 COLLATE utf8_bin NULL COMMENT '郵遞區號'; 
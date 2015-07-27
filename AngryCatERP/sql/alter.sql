-- 2015-07-17
ALTER TABLE `angrycat`.`member` ADD COLUMN `vipDiscountUseDate` DATETIME NULL COMMENT '一年一次會員優惠使用時間' AFTER `fbNickname`;
-- 2015-07-20
ALTER TABLE `angrycat`.`member` ADD COLUMN `toVipEndDate` DATETIME NULL COMMENT 'VIP到期日' AFTER `vipDiscountUseDate`;
ALTER TABLE `angrycat`.`member` CHANGE `postalCode` `postalCode` VARCHAR(100) CHARSET utf8 COLLATE utf8_bin NULL COMMENT '郵遞區號'; 
-- 2015-07-20
ALTER TABLE `angrycat`.`member` ADD COLUMN `toVipEndDate` DATETIME NULL COMMENT 'VIP到期日' AFTER `vipDiscountUseDate`;
ALTER TABLE `angrycat`.`member` CHANGE `postalCode` `postalCode` VARCHAR(100) CHARSET utf8 COLLATE utf8_bin NULL COMMENT '郵遞區號';

-- 2016-01-25
CREATE TABLE `angrycat`.`salesdetail`(  
  `id` VARCHAR(100) NOT NULL,
  `memberId` VARCHAR(100) COMMENT '會員',
  `salePoint` VARCHAR(50) COMMENT '銷售點',
  `saleStatus` VARCHAR(100) COMMENT '銷售狀態',
  `fbName` VARCHAR(500) COMMENT 'FB名稱',
  `activity` VARCHAR(500) COMMENT '活動',
  `modelId` VARCHAR(100) COMMENT '型號',
  `productName` VARCHAR(500) COMMENT '產品名稱',
  `price` DOUBLE COMMENT '價格',
  `memberPrice` DOUBLE COMMENT '會員價',
  `priority` VARCHAR(10) COMMENT '優先順序',
  `orderDate` DATETIME COMMENT '訂購日期',
  `otherNote` VARCHAR(500) COMMENT '其他備註',
  `checkBillStatus` VARCHAR(200) COMMENT '對帳狀態',
  `idNo` VARCHAR(50) COMMENT '身分證字號',
  `discountType` VARCHAR(200) COMMENT '折扣類型',
  `arrivalStatus` VARCHAR(100) COMMENT '到貨狀態',
  `shippingDate` DATETIME COMMENT '出貨日',
  `sendMethod` VARCHAR(200) COMMENT '郵寄方式',
  `note` VARCHAR(500) COMMENT '備註',
  `payDate` DATETIME COMMENT '付款日期',
  `contactInfo` VARCHAR(200) COMMENT '郵寄地址電話',
  `registrant` VARCHAR(100) COMMENT '登單者',
  PRIMARY KEY (`id`)
)
COMMENT='銷售明細';

-- 2016-05-23
CREATE TABLE `shr_module_config` (
  `id` varchar(100) COLLATE utf8_bin NOT NULL,
  `name` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `moduleName` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `json` text COLLATE utf8_bin,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
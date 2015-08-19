-- MySQL dump 10.13  Distrib 5.6.24, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: angrycat
-- ------------------------------------------------------
-- Server version	5.5.5-10.0.19-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*Table structure for table `personalinfo` */

DROP TABLE IF EXISTS `personalinfo`;

CREATE TABLE `personalinfo` (
  `id` varchar(100) COLLATE utf8_bin NOT NULL,
  `name` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `nameEng` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `code` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `idNo` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `country` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `mobile` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `tel` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `ext` varchar(50) COLLATE utf8_bin DEFAULT NULL,
  `address` text COLLATE utf8_bin,
  `fax` varchar(50) COLLATE utf8_bin DEFAULT NULL,
  `email` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `note` text COLLATE utf8_bin,
  `class` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_personalinfo_country` (`country`),
  CONSTRAINT `FK_personalinfo_country` FOREIGN KEY (`country`) REFERENCES `shr_parameter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `member` (
  `id` varchar(100) COLLATE utf8_bin NOT NULL,
  `important` tinyint(2) NOT NULL DEFAULT '0' COMMENT 'VIP',
  `name` varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '姓名',
  `nameEng` varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '英文姓名',
  `gender` tinyint(5) DEFAULT '0',
  `idNo` varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '身分證字號',
  `birthday` datetime DEFAULT NULL COMMENT '出生年月日',
  `email` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `tel` varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '電話',
  `mobile` varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '行動電話',
  `postalCode` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '郵遞區號',
  `address` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '地址',
  `toVipDate` datetime DEFAULT NULL COMMENT '轉VIP日期',
  `note` varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '備註',
  `fbNickname` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT 'FB暱稱',
  `toVipEndDate` datetime DEFAULT NULL COMMENT 'VIP到期日',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='OHM Beads TW (AngryCat) 一般會員資料';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;


DROP TABLE IF EXISTS `vipdiscountdetail`;

CREATE TABLE `vipdiscountdetail` (
  `id` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '資料表識別號',
  `memberId` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '對應會員主表關聯號',
  `memberIdNo` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '會員編號(身分證字號)',
  `effectiveStart` datetime NOT NULL COMMENT '會員有效期起日',
  `effectiveEnd` datetime NOT NULL COMMENT '會員有效期迄日',
  `discountUseDate` datetime DEFAULT NULL COMMENT '使用會員折扣日期',
  `toVipDate` datetime DEFAULT NULL COMMENT '會員資格建立日(轉VIP日)',
  PRIMARY KEY (`id`),
  KEY `fk_memberId` (`memberId`),
  CONSTRAINT `fk_memberId` FOREIGN KEY (`memberId`) REFERENCES `member` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Table structure for table `shr_allgroup`
--

DROP TABLE IF EXISTS `shr_allgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_allgroup` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `parentId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `info` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_shr_allgroup_1` (`parentId`),
  CONSTRAINT `FK_shr_allgroup_1` FOREIGN KEY (`parentId`) REFERENCES `shr_allgroup` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_allgroup`
--

LOCK TABLES `shr_allgroup` WRITE;
/*!40000 ALTER TABLE `shr_allgroup` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_allgroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_allrole`
--

DROP TABLE IF EXISTS `shr_allrole`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_allrole` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_allrole`
--

LOCK TABLES `shr_allrole` WRITE;
/*!40000 ALTER TABLE `shr_allrole` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_allrole` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_alluser`
--

DROP TABLE IF EXISTS `shr_alluser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_alluser` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `info` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `disabled` tinyint(3) unsigned NOT NULL,
  `defaultGroup` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `userId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_alluser_1` (`defaultGroup`),
  CONSTRAINT `FK_alluser_1` FOREIGN KEY (`defaultGroup`) REFERENCES `shr_allgroup` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_alluser`
--

LOCK TABLES `shr_alluser` WRITE;
/*!40000 ALTER TABLE `shr_alluser` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_alluser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_datachangelog`
--

DROP TABLE IF EXISTS `shr_datachangelog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_datachangelog` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `docId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `docType` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `logTime` datetime NOT NULL,
  `userId` varchar(100) NOT NULL,
  `userName` varchar(100) NOT NULL,
  `note` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_datachangelog`
--

LOCK TABLES `shr_datachangelog` WRITE;
/*!40000 ALTER TABLE `shr_datachangelog` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_datachangelog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_datachangelogdetail`
--

DROP TABLE IF EXISTS `shr_datachangelogdetail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_datachangelogdetail` (
  `dataChangeLogId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `idx` int(10) unsigned NOT NULL,
  `fieldName` varchar(100) NOT NULL,
  `originalContent` text,
  `changedContent` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_datachangelogdetail`
--

LOCK TABLES `shr_datachangelogdetail` WRITE;
/*!40000 ALTER TABLE `shr_datachangelogdetail` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_datachangelogdetail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_defaultserial`
--

DROP TABLE IF EXISTS `shr_defaultserial`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_defaultserial` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `sep0` varchar(20) DEFAULT NULL,
  `dateSep0` varchar(10) DEFAULT NULL,
  `sep1` varchar(20) DEFAULT NULL,
  `dateSep1` varchar(10) DEFAULT NULL,
  `sep2` varchar(20) DEFAULT NULL,
  `dateSep2` varchar(10) DEFAULT NULL,
  `sep3` varchar(20) DEFAULT NULL,
  `no` varchar(20) DEFAULT NULL,
  `sep4` varchar(20) DEFAULT NULL,
  `note` varchar(300) DEFAULT NULL,
  `resetNoField` varchar(20) DEFAULT NULL,
  `resetNoFieldLV` varchar(100) DEFAULT NULL,
  `resetNoTo` int(10) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_defaultserial`
--

LOCK TABLES `shr_defaultserial` WRITE;
/*!40000 ALTER TABLE `shr_defaultserial` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_defaultserial` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_emailtemplate`
--

DROP TABLE IF EXISTS `shr_emailtemplate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_emailtemplate` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `code` varchar(100) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `toAddress` text,
  `ccAddress` text,
  `fromAddress` text,
  `subject` text,
  `content` text,
  `attachment` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_emailtemplate`
--

LOCK TABLES `shr_emailtemplate` WRITE;
/*!40000 ALTER TABLE `shr_emailtemplate` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_emailtemplate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_exportcolumn`
--

DROP TABLE IF EXISTS `shr_exportcolumn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_exportcolumn` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `category` varchar(200) NOT NULL,
  `name` varchar(100) NOT NULL,
  `columns` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_exportcolumn`
--

LOCK TABLES `shr_exportcolumn` WRITE;
/*!40000 ALTER TABLE `shr_exportcolumn` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_exportcolumn` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_group_user`
--

DROP TABLE IF EXISTS `shr_group_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_group_user` (
  `groupId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `userId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`groupId`,`userId`),
  KEY `FK_group_user_2` (`userId`),
  CONSTRAINT `FK_group_user_1` FOREIGN KEY (`groupId`) REFERENCES `shr_allgroup` (`id`),
  CONSTRAINT `FK_group_user_2` FOREIGN KEY (`userId`) REFERENCES `shr_alluser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_group_user`
--

LOCK TABLES `shr_group_user` WRITE;
/*!40000 ALTER TABLE `shr_group_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_group_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_indexdocument`
--

DROP TABLE IF EXISTS `shr_indexdocument`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_indexdocument` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `parent` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `docId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `docType` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `fullContent` text,
  `displayTitle` varchar(300) DEFAULT NULL,
  `owner` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ownedGroup` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_indexdocument_1` (`owner`),
  KEY `Index_3` (`docId`),
  KEY `FK_shr_indexdocument_2` (`ownedGroup`),
  KEY `FK_shr_indexdocument_3` (`parent`),
  CONSTRAINT `FK_indexdocument_1` FOREIGN KEY (`owner`) REFERENCES `shr_alluser` (`id`),
  CONSTRAINT `FK_shr_indexdocument_2` FOREIGN KEY (`ownedGroup`) REFERENCES `shr_allgroup` (`id`),
  CONSTRAINT `FK_shr_indexdocument_3` FOREIGN KEY (`parent`) REFERENCES `shr_indexdocument` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_indexdocument`
--

LOCK TABLES `shr_indexdocument` WRITE;
/*!40000 ALTER TABLE `shr_indexdocument` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_indexdocument` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_parameter`
--

DROP TABLE IF EXISTS `shr_parameter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_parameter` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `code` varchar(100) DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `categoryId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `sequence` int(10) unsigned NOT NULL DEFAULT '0',
  `note` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_parameter_1` (`categoryId`),
  CONSTRAINT `FK_parameter_1` FOREIGN KEY (`categoryId`) REFERENCES `shr_parameter_cat` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_parameter`
--

LOCK TABLES `shr_parameter` WRITE;
/*!40000 ALTER TABLE `shr_parameter` DISABLE KEYS */;
INSERT INTO `shr_parameter` VALUES ('20150616-154539509-NmcjF','pcode_0','pname_0','testItem',0,'pnote_0'),('20150616-154539539-Kiqot','pcode_1','pname_1','testItem',1,'pnote_1'),('20150616-154539549-iKVfy','pcode_2','pname_2','testItem',2,'pnote_2'),('20150616-154539550-hmUyO','pcode_3','pname_3','testItem',3,'pnote_3'),('20150616-154539551-YGTBh','pcode_4','pname_4','testItem',4,'pnote_4'),('20150616-154539552-drIed','pcode_5','pname_5','testItem',5,'pnote_5'),('20150616-154539553-gUKxA','pcode_6','pname_6','testItem',6,'pnote_6'),('20150616-154539554-UjcZu','pcode_7','pname_7','testItem',7,'pnote_7'),('20150616-154539555-KoHlJ','pcode_8','pname_8','testItem',8,'pnote_8'),('20150616-154539556-HJZte','pcode_9','pname_9','testItem',9,'pnote_9'),('20150616-154539557-wuhdQ','pcode_10','pname_10','testItem',10,'pnote_10'),('20150616-154539558-Wkspq','pcode_11','pname_11','testItem',11,'pnote_11'),('20150616-154539559-obcGn','pcode_12','pname_12','testItem',12,'pnote_12'),('20150616-154539560-TJIim','pcode_13','pname_13','testItem',13,'pnote_13'),('20150616-154539561-gykQC','pcode_14','pname_14','testItem',14,'pnote_14'),('20150616-154539562-qTBOT','pcode_15','pname_15','testItem',15,'pnote_15'),('20150616-154539563-kZGCO','pcode_16','pname_16','testItem',16,'pnote_16'),('20150616-154539564-DvHQg','pcode_17','pname_17','testItem',17,'pnote_17'),('20150616-154539565-AdEnv','pcode_18','pname_18','testItem',18,'pnote_18'),('20150616-154539566-ILdgm','pcode_19','pname_19','testItem',19,'pnote_19'),('20150616-154539567-WvZTb','pcode_20','pname_20','testItem',20,'pnote_20'),('20150616-154539568-RFHvV','pcode_21','pname_21','testItem',21,'pnote_21'),('20150616-154539569-nFcKf','pcode_22','pname_22','testItem',22,'pnote_22'),('20150616-154539570-FYqtK','pcode_23','pname_23','testItem',23,'pnote_23'),('20150616-154539571-tKshg','pcode_24','pname_24','testItem',24,'pnote_24'),('20150616-154539572-kNKgE','pcode_25','pname_25','testItem',25,'pnote_25'),('20150616-154539573-AJCPU','pcode_26','pname_26','testItem',26,'pnote_26'),('20150616-154539574-AASbc','pcode_27','pname_27','testItem',27,'pnote_27'),('20150616-154539575-ZIKGy','pcode_28','pname_28','testItem',28,'pnote_28'),('20150616-154539576-yXiNR','pcode_29','pname_29','testItem',29,'pnote_29');
/*!40000 ALTER TABLE `shr_parameter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_parameter_cat`
--

DROP TABLE IF EXISTS `shr_parameter_cat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_parameter_cat` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `name` varchar(100) NOT NULL,
  `type` int(4) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_parameter_cat`
--

LOCK TABLES `shr_parameter_cat` WRITE;
/*!40000 ALTER TABLE `shr_parameter_cat` DISABLE KEYS */;
INSERT INTO `shr_parameter_cat` VALUES ('testItem','測試項目',0);
/*!40000 ALTER TABLE `shr_parameter_cat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_parameter_i18n`
--

DROP TABLE IF EXISTS `shr_parameter_i18n`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_parameter_i18n` (
  `parameterId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `localString` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `name` varchar(300) NOT NULL,
  PRIMARY KEY (`parameterId`,`localString`),
  CONSTRAINT `FK_shr_parameter_i18n_1` FOREIGN KEY (`parameterId`) REFERENCES `shr_parameter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_parameter_i18n`
--

LOCK TABLES `shr_parameter_i18n` WRITE;
/*!40000 ALTER TABLE `shr_parameter_i18n` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_parameter_i18n` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_parameter_next`
--

DROP TABLE IF EXISTS `shr_parameter_next`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_parameter_next` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `nextId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `idx` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`,`nextId`),
  KEY `FK_shr_parameter_next_2` (`nextId`),
  CONSTRAINT `FK_shr_parameter_next_1` FOREIGN KEY (`id`) REFERENCES `shr_parameter` (`id`),
  CONSTRAINT `FK_shr_parameter_next_2` FOREIGN KEY (`nextId`) REFERENCES `shr_parameter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_parameter_next`
--

LOCK TABLES `shr_parameter_next` WRITE;
/*!40000 ALTER TABLE `shr_parameter_next` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_parameter_next` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_printtemplate`
--

DROP TABLE IF EXISTS `shr_printtemplate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_printtemplate` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `code` varchar(100) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `note` text,
  `kind` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `kind1` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `kind2` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `templateFile` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_shr_printtemplate_1` (`kind`),
  KEY `FK_shr_printtemplate_2` (`kind1`),
  KEY `FK_shr_printtemplate_3` (`kind2`),
  KEY `FK_shr_printtemplate_4` (`templateFile`),
  CONSTRAINT `FK_shr_printtemplate_1` FOREIGN KEY (`kind`) REFERENCES `shr_parameter` (`id`),
  CONSTRAINT `FK_shr_printtemplate_2` FOREIGN KEY (`kind1`) REFERENCES `shr_parameter` (`id`),
  CONSTRAINT `FK_shr_printtemplate_3` FOREIGN KEY (`kind2`) REFERENCES `shr_parameter` (`id`),
  CONSTRAINT `FK_shr_printtemplate_4` FOREIGN KEY (`templateFile`) REFERENCES `shr_storagefile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_printtemplate`
--

LOCK TABLES `shr_printtemplate` WRITE;
/*!40000 ALTER TABLE `shr_printtemplate` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_printtemplate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_role_group`
--

DROP TABLE IF EXISTS `shr_role_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_role_group` (
  `roleId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `groupId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`roleId`,`groupId`),
  KEY `FK_role_group_2` (`groupId`),
  CONSTRAINT `FK_role_group_1` FOREIGN KEY (`roleId`) REFERENCES `shr_allrole` (`id`),
  CONSTRAINT `FK_role_group_2` FOREIGN KEY (`groupId`) REFERENCES `shr_allgroup` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_role_group`
--

LOCK TABLES `shr_role_group` WRITE;
/*!40000 ALTER TABLE `shr_role_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_role_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_role_user`
--

DROP TABLE IF EXISTS `shr_role_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_role_user` (
  `roleId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `userId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`roleId`,`userId`),
  KEY `FK_role_user_2` (`userId`),
  CONSTRAINT `FK_role_user_1` FOREIGN KEY (`roleId`) REFERENCES `shr_allrole` (`id`),
  CONSTRAINT `FK_role_user_2` FOREIGN KEY (`userId`) REFERENCES `shr_alluser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_role_user`
--

LOCK TABLES `shr_role_user` WRITE;
/*!40000 ALTER TABLE `shr_role_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_role_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_simpleaclentry`
--

DROP TABLE IF EXISTS `shr_simpleaclentry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_simpleaclentry` (
  `docId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `actUser` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `actGroup` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `level` int(10) unsigned NOT NULL,
  `idx` int(10) unsigned DEFAULT NULL,
  KEY `Index_1` (`docId`),
  KEY `FK_simpleaclentry_1` (`actUser`),
  KEY `FK_simpleaclentry_2` (`actGroup`),
  CONSTRAINT `FK_simpleaclentry_1` FOREIGN KEY (`actUser`) REFERENCES `shr_alluser` (`id`),
  CONSTRAINT `FK_simpleaclentry_2` FOREIGN KEY (`actGroup`) REFERENCES `shr_allgroup` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_simpleaclentry`
--

LOCK TABLES `shr_simpleaclentry` WRITE;
/*!40000 ALTER TABLE `shr_simpleaclentry` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_simpleaclentry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shr_storagefile`
--

DROP TABLE IF EXISTS `shr_storagefile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shr_storagefile` (
  `id` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `name` varchar(300) DEFAULT NULL,
  `docId` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `contentType` varchar(100) DEFAULT NULL,
  `docType` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `note` varchar(500) DEFAULT NULL,
  `path` varchar(512) DEFAULT NULL,
  `rawData` longblob,
  `col` varchar(45) DEFAULT NULL,
  `createTime` datetime DEFAULT NULL,
  `fsize` bigint(20) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `Index_2` (`docId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shr_storagefile`
--

LOCK TABLES `shr_storagefile` WRITE;
/*!40000 ALTER TABLE `shr_storagefile` DISABLE KEYS */;
/*!40000 ALTER TABLE `shr_storagefile` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-06-17 11:29:57

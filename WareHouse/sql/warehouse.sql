/*
 Navicat Premium Data Transfer

 Source Server         : warehouse
 Source Server Type    : MySQL
 Source Server Version : 50729
 Source Host           : localhost:3306
 Source Schema         : warehouse

 Target Server Type    : MySQL
 Target Server Version : 50729
 File Encoding         : 65001

 Date: 08/05/2020 09:38:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for approve
-- ----------------------------
DROP TABLE IF EXISTS `approve`;
CREATE TABLE `approve`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `approveName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审批人',
  `approveSuccessTime` datetime(0) NULL DEFAULT NULL COMMENT '审批成功时间',
  `inportId` int(11) NULL DEFAULT NULL COMMENT '进货id',
  `outportId` int(11) NULL DEFAULT NULL COMMENT '退货id',
  `salesId` int(11) NULL DEFAULT NULL COMMENT '销售id',
  `salsebackId` int(11) NULL DEFAULT NULL COMMENT '销售退后id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `pk_inportId`(`inportId`) USING BTREE,
  INDEX `pk_outportId`(`outportId`) USING BTREE,
  INDEX `pk_salesId`(`salesId`) USING BTREE,
  INDEX `pk_salesbackId`(`salsebackId`) USING BTREE,
  CONSTRAINT `pk_inportId` FOREIGN KEY (`inportId`) REFERENCES `inport` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `pk_outportId` FOREIGN KEY (`outportId`) REFERENCES `outport` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `pk_salesId` FOREIGN KEY (`salesId`) REFERENCES `sales` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `pk_salesbackId` FOREIGN KEY (`salsebackId`) REFERENCES `salesback` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '审批表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of approve
-- ----------------------------
INSERT INTO `approve` VALUES (1, '程瑞东', '2020-02-06 15:58:08', NULL, NULL, 2, NULL);
INSERT INTO `approve` VALUES (2, '程瑞东', '2020-02-06 16:42:44', 1, NULL, NULL, NULL);
INSERT INTO `approve` VALUES (3, '程瑞东', '2020-02-06 16:50:40', 2, NULL, NULL, NULL);
INSERT INTO `approve` VALUES (4, '程瑞东', '2020-02-07 11:33:01', NULL, 1, NULL, NULL);
INSERT INTO `approve` VALUES (5, '黄佳乐', '2020-02-09 16:28:21', NULL, NULL, 3, NULL);
INSERT INTO `approve` VALUES (6, '黄佳乐', '2020-02-09 17:17:34', 4, NULL, NULL, NULL);
INSERT INTO `approve` VALUES (7, '黄佳乐', '2020-02-09 17:35:53', NULL, 2, NULL, NULL);
INSERT INTO `approve` VALUES (8, '黄佳乐', '2020-02-09 18:05:30', NULL, NULL, 4, NULL);
INSERT INTO `approve` VALUES (9, '黄佳乐', '2020-02-09 18:37:09', NULL, NULL, 5, NULL);
INSERT INTO `approve` VALUES (10, '黄佳乐', '2020-02-09 18:42:22', NULL, NULL, NULL, 3);
INSERT INTO `approve` VALUES (11, '黄佳乐', '2020-02-09 18:51:25', NULL, NULL, 6, NULL);
INSERT INTO `approve` VALUES (12, '黄佳乐', '2020-02-10 10:52:07', 6, NULL, NULL, NULL);
INSERT INTO `approve` VALUES (13, '黄佳乐', '2020-02-10 10:54:33', NULL, 3, NULL, NULL);
INSERT INTO `approve` VALUES (14, '黄佳乐', '2020-02-10 10:58:21', NULL, NULL, 6, NULL);
INSERT INTO `approve` VALUES (15, '黄佳乐', '2020-02-11 14:24:21', NULL, NULL, 6, NULL);
INSERT INTO `approve` VALUES (16, '黄佳乐', '2020-02-11 14:37:17', NULL, NULL, 6, NULL);
INSERT INTO `approve` VALUES (17, '黄佳乐', '2020-02-11 14:37:26', NULL, NULL, 6, NULL);
INSERT INTO `approve` VALUES (18, '黄佳乐', '2020-02-11 14:38:22', NULL, NULL, 7, NULL);
INSERT INTO `approve` VALUES (19, '黄佳乐', '2020-04-07 09:10:09', 3, NULL, NULL, NULL);
INSERT INTO `approve` VALUES (20, '黄佳乐', '2020-04-07 09:10:15', 5, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for customar
-- ----------------------------
DROP TABLE IF EXISTS `customar`;
CREATE TABLE `customar`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customerName` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '客户全称',
  `zip` char(6) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `tel` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `address` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `contactName` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `contactTel` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `bank` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `account` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `state` int(11) NOT NULL COMMENT '0不可用，1可用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '客户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customar
-- ----------------------------
INSERT INTO `customar` VALUES (1, '安徽合肥一元北大青鸟计算机培训机构', '000000', '18377664455', '安徽省合肥市庐阳区', '周潜明', '15322336688', '农业银行', '6228486544313313434', '511536322@qq.com', 1);
INSERT INTO `customar` VALUES (2, '华联超市', '000000', '18297397537', '安徽省亳州市蒙城县', '黄佳乐', '18297397537', '中国银行', '654431331343413', '1652161147@qq.com', 1);
INSERT INTO `customar` VALUES (3, '大润发', '000000', '16745653355', '安徽合肥市庐阳区', '小发', '18297676543', '中国银行', '654233455534544', '165234@qq.com', 1);

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES (1, '销售部', '');
INSERT INTO `department` VALUES (2, '进货部', NULL);
INSERT INTO `department` VALUES (3, '仓管部', NULL);
INSERT INTO `department` VALUES (4, '后勤部', NULL);
INSERT INTO `department` VALUES (5, '董事会', NULL);
INSERT INTO `department` VALUES (6, '技术部', '');

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `goodsName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `producePlace` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产地',
  `goodsType` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '规格',
  `packageId` int(11) NULL DEFAULT NULL COMMENT '包装',
  `productCode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '生产批号',
  `promitCode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '批准文字',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `inportprice` double(10, 2) NULL DEFAULT NULL COMMENT '进货价格',
  `salesprice` double(10, 2) NULL DEFAULT NULL COMMENT '销售价格',
  `providerId` int(10) NULL DEFAULT NULL COMMENT '供应商编号',
  `state` int(10) NULL DEFAULT NULL,
  `number` int(11) NULL DEFAULT NULL COMMENT '数量',
  `goodsImg` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片',
  `wareId` int(11) NULL DEFAULT NULL COMMENT '仓库id',
  `locationId` int(11) NULL DEFAULT NULL COMMENT '库位id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `pk_pid`(`providerId`) USING BTREE,
  INDEX `pk_wid`(`wareId`) USING BTREE,
  INDEX `pk_lid`(`locationId`) USING BTREE,
  CONSTRAINT `pk_lid` FOREIGN KEY (`locationId`) REFERENCES `location` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `pk_pid` FOREIGN KEY (`providerId`) REFERENCES `provider` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `pk_wid` FOREIGN KEY (`wareId`) REFERENCES `ware` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, '娃哈哈', '杭州 ', '饮料', '120ml', 1, 'PH12345', 'PZ1234', '小孩子都爱的', 1.00, 2.00, 3, 1, 300, 'img/avatar.png', 1, 1);
INSERT INTO `goods` VALUES (2, '旺旺雪饼', '仙桃', '零食', '包', 2, 'PZ12312312', 'PZ12312312', '小孩子都爱的吃', 3.00, 5.00, 1, 1, 250, 'img/avatar.png', 1, 1);
INSERT INTO `goods` VALUES (3, '农夫山泉', '浙江省杭州市', '饮料', '380ml', 1, 'PH12345', 'PZ1234', '农夫山泉有点甜', 1.00, 2.00, 3, 1, 100, 'img/avatar.png', 1, 1);
INSERT INTO `goods` VALUES (4, '香辣牛肉面(袋)', '合肥', '零食', '箱', 4, 'PZ123', 'PZ123', '原汁原味', 30.00, 38.00, 2, 1, 70, 'img/0c0e1d5a-36ab-4516-a9fa-0d95795d9718.jpg', 1, 1);
INSERT INTO `goods` VALUES (5, '香辣牛肉面(桶)', '合肥', '零食', '箱', 4, 'PZ12345', 'PZ12345', '味道不错', 40.00, 50.00, 1, 1, 100, 'img/808c2487-b2bf-4300-a2f1-027929afaeca.jpg', 1, 1);
INSERT INTO `goods` VALUES (6, '红烧牛肉面(袋)', '亳州', '零食', '箱', 4, 'PZ12345', 'PZ12345', '鲜美', 30.00, 40.00, 1, 1, 120, 'img/avatar.png', 1, 1);
INSERT INTO `goods` VALUES (7, '红烧牛肉面(桶)', '亳州', '零食', '箱', 4, 'PZ12345', 'PZ12345', '鲜美', 41.00, 55.00, 1, 1, 120, 'img/avatar.png', 1, 1);
INSERT INTO `goods` VALUES (8, '红牛', '武汉', '饮料', '250ml', 1, 'PZ1234', 'PZ1234', '提神醒脑', 3.50, 5.00, 1, 1, 50, 'img/78c082b7-0ebe-4fb6-810f-21f2d9007832.jpg', 2, 1);
INSERT INTO `goods` VALUES (9, '康师傅冰红茶', '合肥', '饮料', '380ml', 1, 'PZ1234', 'PZ1234', '老牌饮料', 2.00, 3.00, 3, 1, 100, 'img/avatar.png', 1, 2);
INSERT INTO `goods` VALUES (10, '康师傅绿茶', '安徽合肥', '饮料', '380ml', 1, '32434322', '32434322', '好喝', 3.00, 4.00, 2, 1, 100, 'img/d87a4665-7ebb-40cc-812b-f6bc217064f8.jpg', 1, 1);

-- ----------------------------
-- Table structure for inport
-- ----------------------------
DROP TABLE IF EXISTS `inport`;
CREATE TABLE `inport`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `providerId` int(11) NULL DEFAULT NULL,
  `payType` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '1.微信2.支付宝3.现金',
  ` approveTime` datetime(0) NULL DEFAULT NULL COMMENT '提交审批时间',
  `inportTime` datetime(0) NULL DEFAULT NULL COMMENT '入库时间',
  `operateName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '进货人',
  `warehouseName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '仓管人员',
  `number` int(11) NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `inportPrice` double(11, 2) NULL DEFAULT NULL COMMENT '进货价格',
  `goodsId` int(11) NULL DEFAULT NULL COMMENT '商品编号',
  `state` int(11) NULL DEFAULT NULL,
  `reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '进货原因',
  `goodsSize` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `pk_ipid`(`providerId`) USING BTREE,
  INDEX `pk_igid`(`goodsId`) USING BTREE,
  CONSTRAINT `pk_igid` FOREIGN KEY (`goodsId`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `pk_ipid` FOREIGN KEY (`providerId`) REFERENCES `provider` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '进货表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inport
-- ----------------------------
INSERT INTO `inport` VALUES (1, 1, '微信', '2020-02-06 15:14:58', '2020-02-06 16:45:43', '黄佳乐', '程瑞东', 50, '', 3.00, 2, 2, NULL, '12个');
INSERT INTO `inport` VALUES (2, 3, '微信', '2020-02-06 16:50:05', '2020-02-06 16:51:21', '程瑞东', '程瑞东', 100, '', 1.00, 1, 2, '库存不足', '120ml');
INSERT INTO `inport` VALUES (3, 3, '微信', '2020-02-07 11:17:01', NULL, '黄佳乐', NULL, 50, '', 1.00, 1, 1, '', '120ml');
INSERT INTO `inport` VALUES (4, 1, '微信', '2020-02-09 17:17:04', '2020-02-09 17:18:03', '华起明', '程瑞东', 100, '', 40.00, 5, 2, '库存不足', '箱');
INSERT INTO `inport` VALUES (5, 3, '', '2020-02-09 17:41:24', NULL, '华起明', NULL, 100, '', 3.00, 8, 1, '库存不足', '250ml');
INSERT INTO `inport` VALUES (6, 3, '微信', '2020-02-10 10:51:37', '2020-02-10 10:52:20', '华起明', '程瑞东', 100, '', 4.00, 8, 2, '库存不足', '250ml');

-- ----------------------------
-- Table structure for location
-- ----------------------------
DROP TABLE IF EXISTS `location`;
CREATE TABLE `location`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `locationName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `wareId` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '库位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of location
-- ----------------------------
INSERT INTO `location` VALUES (1, '1区', 1);
INSERT INTO `location` VALUES (2, '2区', 1);
INSERT INTO `location` VALUES (3, '3区', 1);
INSERT INTO `location` VALUES (4, '1区', 2);
INSERT INTO `location` VALUES (5, '库位1', 4);
INSERT INTO `location` VALUES (6, '库位2', 4);
INSERT INTO `location` VALUES (7, '库位3', 4);
INSERT INTO `location` VALUES (8, '库位4', 4);
INSERT INTO `location` VALUES (9, '库位5', 4);
INSERT INTO `location` VALUES (10, '库位1', 5);
INSERT INTO `location` VALUES (11, '库位2', 5);
INSERT INTO `location` VALUES (12, '库位3', 5);
INSERT INTO `location` VALUES (13, '库位4', 5);

-- ----------------------------
-- Table structure for log
-- ----------------------------
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录人\r\n',
  `ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'ip地址',
  `time` datetime(0) NULL DEFAULT NULL COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 73 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of log
-- ----------------------------
INSERT INTO `log` VALUES (1, '黄佳乐', '127.0.0.1', '2020-02-09 14:09:00');
INSERT INTO `log` VALUES (2, '黄佳乐', '127.0.0.1', '2020-02-09 14:09:00');
INSERT INTO `log` VALUES (3, '华起明', '127.0.0.1', '2020-02-09 14:10:10');
INSERT INTO `log` VALUES (4, '黄佳乐', '127.0.0.1', '2020-02-09 15:31:35');
INSERT INTO `log` VALUES (6, '黄佳乐', '127.0.0.1', '2020-02-09 15:49:46');
INSERT INTO `log` VALUES (7, '黄佳乐', '127.0.0.1', '2020-02-10 10:37:17');
INSERT INTO `log` VALUES (8, '程瑞东', '127.0.0.1', '2020-02-10 10:49:20');
INSERT INTO `log` VALUES (9, '华起明', '127.0.0.1', '2020-02-10 10:49:57');
INSERT INTO `log` VALUES (10, '于永波', '127.0.0.1', '2020-02-10 10:55:34');
INSERT INTO `log` VALUES (11, '黄佳乐', '127.0.0.1', '2020-02-10 10:59:31');
INSERT INTO `log` VALUES (12, '黄佳乐', '127.0.0.1', '2020-02-10 11:00:23');
INSERT INTO `log` VALUES (13, '黄佳乐', '127.0.0.1', '2020-02-10 11:03:49');
INSERT INTO `log` VALUES (14, '黄佳乐', '127.0.0.1', '2020-02-10 11:37:20');
INSERT INTO `log` VALUES (15, '黄佳乐', '127.0.0.1', '2020-02-10 15:24:09');
INSERT INTO `log` VALUES (16, '黄佳乐', '127.0.0.1', '2020-02-10 15:27:44');
INSERT INTO `log` VALUES (17, '黄佳乐', '127.0.0.1', '2020-02-11 14:23:44');
INSERT INTO `log` VALUES (18, '黄佳乐', '127.0.0.1', '2020-02-11 14:23:44');
INSERT INTO `log` VALUES (19, '黄佳乐', '127.0.0.1', '2020-02-11 14:31:33');
INSERT INTO `log` VALUES (20, '黄佳乐', '127.0.0.1', '2020-02-11 14:37:10');
INSERT INTO `log` VALUES (21, '黄佳乐', '127.0.0.1', '2020-02-11 14:38:04');
INSERT INTO `log` VALUES (22, '黄佳乐', '127.0.0.1', '2020-02-28 14:37:51');
INSERT INTO `log` VALUES (23, '黄佳乐', '127.0.0.1', '2020-04-04 12:48:13');
INSERT INTO `log` VALUES (24, '黄佳乐', '127.0.0.1', '2020-04-07 08:51:58');
INSERT INTO `log` VALUES (25, '黄佳乐', '127.0.0.1', '2020-04-07 09:08:20');
INSERT INTO `log` VALUES (26, '黄佳乐', '127.0.0.1', '2020-04-07 18:05:58');
INSERT INTO `log` VALUES (27, '黄佳乐', '127.0.0.1', '2020-04-07 18:10:02');
INSERT INTO `log` VALUES (28, '黄佳乐', '127.0.0.1', '2020-04-07 18:10:05');
INSERT INTO `log` VALUES (29, '黄佳乐', '127.0.0.1', '2020-04-07 18:10:11');
INSERT INTO `log` VALUES (30, '黄佳乐', '127.0.0.1', '2020-04-07 18:22:27');
INSERT INTO `log` VALUES (31, '黄佳乐', '127.0.0.1', '2020-04-07 18:24:09');
INSERT INTO `log` VALUES (32, '黄佳乐', '127.0.0.1', '2020-04-08 09:07:05');
INSERT INTO `log` VALUES (33, '黄佳乐', '127.0.0.1', '2020-04-08 09:07:07');
INSERT INTO `log` VALUES (34, '黄佳乐', '127.0.0.1', '2020-04-08 09:07:14');
INSERT INTO `log` VALUES (35, '黄佳乐', '127.0.0.1', '2020-04-08 09:07:52');
INSERT INTO `log` VALUES (36, '黄佳乐', '127.0.0.1', '2020-04-08 09:07:53');
INSERT INTO `log` VALUES (37, '黄佳乐', '127.0.0.1', '2020-04-08 09:07:54');
INSERT INTO `log` VALUES (38, '黄佳乐', '127.0.0.1', '2020-04-08 09:07:55');
INSERT INTO `log` VALUES (39, '黄佳乐', '127.0.0.1', '2020-04-08 09:07:55');
INSERT INTO `log` VALUES (40, '黄佳乐', '127.0.0.1', '2020-04-08 09:19:36');
INSERT INTO `log` VALUES (41, '黄佳乐', '127.0.0.1', '2020-04-08 09:19:59');
INSERT INTO `log` VALUES (42, '黄佳乐', '127.0.0.1', '2020-04-08 09:29:19');
INSERT INTO `log` VALUES (43, '黄佳乐', '127.0.0.1', '2020-04-08 09:32:36');
INSERT INTO `log` VALUES (44, '黄佳乐', '127.0.0.1', '2020-04-08 09:35:52');
INSERT INTO `log` VALUES (45, '黄佳乐', '127.0.0.1', '2020-04-13 21:37:10');
INSERT INTO `log` VALUES (46, '黄佳乐', '127.0.0.1', '2020-04-14 20:28:20');
INSERT INTO `log` VALUES (47, '黄佳乐', '127.0.0.1', '2020-04-16 19:52:16');
INSERT INTO `log` VALUES (48, '华起明', '127.0.0.1', '2020-04-16 19:55:03');
INSERT INTO `log` VALUES (49, '于永波', '127.0.0.1', '2020-04-16 19:56:12');
INSERT INTO `log` VALUES (50, '程瑞东', '127.0.0.1', '2020-04-16 19:56:49');
INSERT INTO `log` VALUES (51, '黄佳乐', '127.0.0.1', '2020-04-16 20:02:22');
INSERT INTO `log` VALUES (52, '黄佳乐', '127.0.0.1', '2020-04-18 13:41:55');
INSERT INTO `log` VALUES (53, '黄佳乐', '127.0.0.1', '2020-04-18 15:08:00');
INSERT INTO `log` VALUES (54, '黄佳乐', '127.0.0.1', '2020-04-18 15:33:29');
INSERT INTO `log` VALUES (55, '黄佳乐', '127.0.0.1', '2020-04-18 15:35:01');
INSERT INTO `log` VALUES (56, '黄佳乐', '127.0.0.1', '2020-04-18 15:36:40');
INSERT INTO `log` VALUES (57, '黄佳乐', '127.0.0.1', '2020-04-18 15:39:48');
INSERT INTO `log` VALUES (58, '黄佳乐', '127.0.0.1', '2020-04-18 15:42:25');
INSERT INTO `log` VALUES (59, '黄佳乐', '127.0.0.1', '2020-04-18 15:44:59');
INSERT INTO `log` VALUES (60, '黄佳乐', '127.0.0.1', '2020-04-18 15:46:20');
INSERT INTO `log` VALUES (61, '黄佳乐', '127.0.0.1', '2020-04-18 15:48:11');
INSERT INTO `log` VALUES (62, '黄佳乐', '127.0.0.1', '2020-04-18 15:54:24');
INSERT INTO `log` VALUES (63, '黄佳乐', '127.0.0.1', '2020-04-18 16:04:54');
INSERT INTO `log` VALUES (64, '黄佳乐', '127.0.0.1', '2020-04-18 16:06:23');
INSERT INTO `log` VALUES (65, '黄佳乐', '127.0.0.1', '2020-04-18 16:06:33');
INSERT INTO `log` VALUES (66, '黄佳乐', '127.0.0.1', '2020-04-18 16:09:23');
INSERT INTO `log` VALUES (67, '黄佳乐', '127.0.0.1', '2020-04-18 16:12:18');
INSERT INTO `log` VALUES (68, '黄佳乐', '127.0.0.1', '2020-04-18 16:12:28');
INSERT INTO `log` VALUES (69, '黄佳乐', '127.0.0.1', '2020-04-18 16:15:02');
INSERT INTO `log` VALUES (70, '黄佳乐', '127.0.0.1', '2020-04-18 16:17:16');
INSERT INTO `log` VALUES (71, '黄佳乐', '127.0.0.1', '2020-04-20 09:39:09');
INSERT INTO `log` VALUES (72, '黄佳乐', '127.0.0.1', '2020-04-20 09:39:46');

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标题',
  `context` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '内容',
  `time` date NULL DEFAULT NULL COMMENT '时间',
  `operateName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发布人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (3, '周一项目汇报', '都到', '2020-02-10', '黄佳乐');
INSERT INTO `notice` VALUES (4, '测试公告', '威尔士大所多', '2020-04-18', '黄佳乐');

-- ----------------------------
-- Table structure for outport
-- ----------------------------
DROP TABLE IF EXISTS `outport`;
CREATE TABLE `outport`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `providerId` int(11) NULL DEFAULT NULL,
  `payType` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `outputTime` datetime(0) NULL DEFAULT NULL COMMENT '出库时间',
  `operateName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `number` int(11) NULL DEFAULT NULL,
  `goodsId` int(11) NULL DEFAULT NULL,
  `reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '原因',
  `approveTime` datetime(0) NULL DEFAULT NULL COMMENT '提交审批时间',
  `warehouseName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '仓管人员',
  `state` int(11) NULL DEFAULT NULL,
  `outputPrice` double(10, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `pk_opid`(`providerId`) USING BTREE,
  INDEX `pk_ogid`(`goodsId`) USING BTREE,
  CONSTRAINT `pk_ogid` FOREIGN KEY (`goodsId`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `pk_opid` FOREIGN KEY (`providerId`) REFERENCES `provider` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '退货表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of outport
-- ----------------------------
INSERT INTO `outport` VALUES (1, 1, '微信', '2020-02-07 11:34:38', '华启明', 50, 2, '', '2020-02-07 11:32:44', '程瑞东', 2, 1.00);
INSERT INTO `outport` VALUES (2, 1, '微信', '2020-02-09 17:36:55', '华起明', 100, 5, '损坏', '2020-02-09 17:33:55', '程瑞东', 2, 40.00);
INSERT INTO `outport` VALUES (3, 3, '', '2020-02-10 10:54:47', '华起明', 50, 8, '过期', '2020-02-10 10:54:08', '程瑞东', 2, 3.00);

-- ----------------------------
-- Table structure for package
-- ----------------------------
DROP TABLE IF EXISTS `package`;
CREATE TABLE `package`  (
  `id` int(11) NOT NULL,
  `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商品包装表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of package
-- ----------------------------
INSERT INTO `package` VALUES (1, '瓶');
INSERT INTO `package` VALUES (2, '包');
INSERT INTO `package` VALUES (3, '袋');
INSERT INTO `package` VALUES (4, '盒');
INSERT INTO `package` VALUES (5, '箱');

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `p_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `state` int(11) NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permission
-- ----------------------------
INSERT INTO `permission` VALUES (1, '员工管理', 'staff:staff', 1, '');
INSERT INTO `permission` VALUES (2, '角色管理', 'role:role', 1, '');
INSERT INTO `permission` VALUES (3, '客户管理', 'customar:customar', 1, NULL);
INSERT INTO `permission` VALUES (4, '供应商管理', 'provider:provider', 1, NULL);
INSERT INTO `permission` VALUES (5, '商品管理', 'goods:goods', 1, NULL);
INSERT INTO `permission` VALUES (6, '仓库管理', 'ware:ware', 1, NULL);
INSERT INTO `permission` VALUES (7, '商品进货', 'getProviderList:getProviderList', 1, NULL);
INSERT INTO `permission` VALUES (8, '商品退货', 'getOutportList:getOutportList', 1, NULL);
INSERT INTO `permission` VALUES (9, '审批商品进货', 'getProviderList:getProviderList', 1, NULL);
INSERT INTO `permission` VALUES (10, '审批商品退货', 'getOutportList:getOutportList', 1, NULL);
INSERT INTO `permission` VALUES (11, '商品进货进库', 'getApprovedInportList:getApprovedInportList', 1, NULL);
INSERT INTO `permission` VALUES (12, '商品退货出库', 'getApprovedOutportList:getApprovedOutportList', 1, NULL);
INSERT INTO `permission` VALUES (13, '销售商品', 'salesList:salesList', 1, NULL);
INSERT INTO `permission` VALUES (14, '销售商品退货', 'getSalesBack:getSalesBack', 1, NULL);
INSERT INTO `permission` VALUES (15, '销售表单审批', 'salesFromList:salesFromList', 1, NULL);
INSERT INTO `permission` VALUES (16, '销售表单执行', 'salesFromList:salesFromList', 1, NULL);
INSERT INTO `permission` VALUES (17, '部门管理', 'Dapartment:Dapartment', 1, NULL);
INSERT INTO `permission` VALUES (18, '权限管理', 'Permission:Permission', 1, NULL);

-- ----------------------------
-- Table structure for provider
-- ----------------------------
DROP TABLE IF EXISTS `provider`;
CREATE TABLE `provider`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `providername` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '供应商全称',
  `zip` char(6) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '邮编',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公司地址',
  `tel` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `contactname` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'l联系人',
  `contacttel` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `bank` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '开户银行',
  `account` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '银行账号',
  `email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '联系人邮箱',
  `state` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '供应商表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of provider
-- ----------------------------
INSERT INTO `provider` VALUES (1, '旺旺食品', '434000', '仙桃', '13413441141', '小明', '13413441141', '中国农业银行', '654124345131', '12312321@sina.com', 1);
INSERT INTO `provider` VALUES (2, '达利园食品', '430000', '汉川', '13413441141', '大明', '13413441141', '中国农业银行', '654124345131', '12312321@sina.com', 1);
INSERT INTO `provider` VALUES (3, '娃哈哈集团', '513131', '杭州', '16543569820', '小晨', '16543569820', '建设银行', '512314141541', '141541@qq.com', 1);

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `state` int(11) NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, '普通员工', 1, '');
INSERT INTO `role` VALUES (2, '仓库管理员', 1, '');
INSERT INTO `role` VALUES (3, '销售管理员 ', 1, '');
INSERT INTO `role` VALUES (4, '进货管理员', 1, '');
INSERT INTO `role` VALUES (5, '超级管理员', 1, '');
INSERT INTO `role` VALUES (6, '测试管理员', 1, '');
INSERT INTO `role` VALUES (8, '测试管理', 1, '');

-- ----------------------------
-- Table structure for role-permission
-- ----------------------------
DROP TABLE IF EXISTS `role-permission`;
CREATE TABLE `role-permission`  (
  `rid` int(11) NULL DEFAULT NULL,
  `pid` int(11) NULL DEFAULT NULL,
  INDEX `pk_id`(`rid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role-permission
-- ----------------------------
INSERT INTO `role-permission` VALUES (2, 1);
INSERT INTO `role-permission` VALUES (2, 2);
INSERT INTO `role-permission` VALUES (2, 1);
INSERT INTO `role-permission` VALUES (3, 1);
INSERT INTO `role-permission` VALUES (3, 2);
INSERT INTO `role-permission` VALUES (3, 3);
INSERT INTO `role-permission` VALUES (6, 1);
INSERT INTO `role-permission` VALUES (6, 2);
INSERT INTO `role-permission` VALUES (6, 3);
INSERT INTO `role-permission` VALUES (6, 4);
INSERT INTO `role-permission` VALUES (6, 5);
INSERT INTO `role-permission` VALUES (6, 6);
INSERT INTO `role-permission` VALUES (6, 7);
INSERT INTO `role-permission` VALUES (6, 8);
INSERT INTO `role-permission` VALUES (8, 1);
INSERT INTO `role-permission` VALUES (8, 2);
INSERT INTO `role-permission` VALUES (8, 3);
INSERT INTO `role-permission` VALUES (8, 4);
INSERT INTO `role-permission` VALUES (8, 5);
INSERT INTO `role-permission` VALUES (8, 6);
INSERT INTO `role-permission` VALUES (8, 9);
INSERT INTO `role-permission` VALUES (8, 10);
INSERT INTO `role-permission` VALUES (8, 11);
INSERT INTO `role-permission` VALUES (8, 12);
INSERT INTO `role-permission` VALUES (8, 13);

-- ----------------------------
-- Table structure for sales
-- ----------------------------
DROP TABLE IF EXISTS `sales`;
CREATE TABLE `sales`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customerId` int(11) NULL DEFAULT NULL COMMENT '客户id',
  `payType` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `salesTime` datetime(0) NULL DEFAULT NULL COMMENT '售卖时间',
  `operateName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '销售人',
  `number` int(11) NULL DEFAULT NULL,
  `salePrice` double(11, 2) NULL DEFAULT NULL COMMENT '售卖价格',
  `goodsId` int(11) NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `warehouseName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '仓管人员',
  `approveTime` datetime(0) NULL DEFAULT NULL COMMENT '提交审批时间',
  `state` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `pk_scid`(`customerId`) USING BTREE,
  INDEX `pk_sgid`(`goodsId`) USING BTREE,
  CONSTRAINT `pk_scid` FOREIGN KEY (`customerId`) REFERENCES `customar` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `pk_sgid` FOREIGN KEY (`goodsId`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '销售表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sales
-- ----------------------------
INSERT INTO `sales` VALUES (2, 1, '微信', '2020-02-06 00:00:00', '于永波', 100, 2.00, 1, '', '程瑞东', '2020-02-06 15:36:24', 2);
INSERT INTO `sales` VALUES (3, 1, '微信', '2020-02-09 00:00:00', '于永波', 30, 38.00, 4, '', '程瑞东', '2020-02-09 16:27:15', 2);
INSERT INTO `sales` VALUES (4, 1, '微信', '2020-02-09 18:13:40', '于永波', 130, 5.00, 8, '', '程瑞东', '2020-02-09 18:04:57', 2);
INSERT INTO `sales` VALUES (5, 2, '微信', '2020-02-09 18:38:17', '于永波', 100, 2.00, 3, '', '程瑞东', '2020-02-09 18:35:57', 2);
INSERT INTO `sales` VALUES (6, 1, '微信', NULL, '黄佳乐', 10, 38.00, 6, '', NULL, '2020-02-09 18:51:09', 1);
INSERT INTO `sales` VALUES (7, 3, '微信', NULL, '于永波', 20, 5.00, 9, '', NULL, '2020-02-10 10:58:03', 1);

-- ----------------------------
-- Table structure for salesback
-- ----------------------------
DROP TABLE IF EXISTS `salesback`;
CREATE TABLE `salesback`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customerId` int(11) NULL DEFAULT NULL,
  `payType` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `salesbackTime` datetime(0) NULL DEFAULT NULL COMMENT '入库时间',
  `operateName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '操作人',
  `number` int(11) NULL DEFAULT NULL,
  `goodsId` int(11) NULL DEFAULT NULL,
  `reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '原因',
  `warehouseName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '仓管人员',
  `approveTime` datetime(0) NULL DEFAULT NULL COMMENT '提交审批时间',
  `state` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `pk_bcid`(`customerId`) USING BTREE,
  INDEX `pk_bgid`(`goodsId`) USING BTREE,
  CONSTRAINT `pk_bcid` FOREIGN KEY (`customerId`) REFERENCES `customar` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `pk_bgid` FOREIGN KEY (`goodsId`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '销售退货表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of salesback
-- ----------------------------
INSERT INTO `salesback` VALUES (1, 1, '银联', '2020-02-06 00:00:00', '于永波', 50, 1, '', NULL, '2020-02-06 15:43:09', 0);
INSERT INTO `salesback` VALUES (2, 1, '微信', '2020-02-06 00:00:00', '于永波', 100, 1, '', NULL, '2020-02-06 15:55:43', 0);
INSERT INTO `salesback` VALUES (3, 2, '微信', '2020-02-09 18:42:42', '于永波', 100, 3, '', '程瑞东', '2020-02-09 18:42:07', 2);

-- ----------------------------
-- Table structure for staff
-- ----------------------------
DROP TABLE IF EXISTS `staff`;
CREATE TABLE `staff`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `jobNumber` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '工号',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `idCard` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `tel` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sex` int(11) NULL DEFAULT NULL,
  `age` int(11) NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '具体地址',
  `entryTime` date NOT NULL COMMENT '入职时间',
  `dapatmentId` int(11) NULL DEFAULT NULL COMMENT '部门id',
  `state` int(11) NULL DEFAULT NULL COMMENT '0已封禁，1已启用',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `pic` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `roleId` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `pk_did`(`dapatmentId`) USING BTREE,
  INDEX `pk_rid`(`roleId`) USING BTREE,
  CONSTRAINT `pk_did` FOREIGN KEY (`dapatmentId`) REFERENCES `department` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `pk_rid` FOREIGN KEY (`roleId`) REFERENCES `role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '员工表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of staff
-- ----------------------------
INSERT INTO `staff` VALUES (1, '黄佳乐', '1001', '81fcf6df343d4910ac7468bc01341218', '341224199708053012', '18297397537', 1, 24, '安徽省蒙城县双涧镇', '2019-12-01', 1, 1, NULL, '2020-02-10\\da6d800e-4b4f-4fee-8e17-19da7c51bd30.jpg', 5);
INSERT INTO `staff` VALUES (2, '华起明', '1002', '567d79fb266c4128333b27a2d817cbd9', '34122419970805301x', '18297397533', 1, 22, '安徽省马鞍山市', '2019-12-19', 2, 1, NULL, 'avatar.png', 4);
INSERT INTO `staff` VALUES (3, '于永波', '1003', '567d79fb266c4128333b27a2d817cbd9', '341224199708053014', '18297397534', 1, 22, '安徽省蚌埠市', '2020-01-01', 1, 1, NULL, 'avatar.png', 3);
INSERT INTO `staff` VALUES (4, '程瑞东', '1004', '567d79fb266c4128333b27a2d817cbd9', '341224199708053014', '18297397535', 1, 21, '安徽省合肥市', '2020-01-15', 3, 1, NULL, 'avatar.png', 2);
INSERT INTO `staff` VALUES (5, '曹亚军', '1005', '567d79fb266c4128333b27a2d817cbd9', '341224199708053014', '18297397536', 1, 25, '安徽省阜阳市', '2020-02-01', 4, 1, NULL, 'avatar.png', 1);
INSERT INTO `staff` VALUES (9, '小黄', '1006', '567d79fb266c4128333b27a2d817cbd9', '3432535353523', '18765456789', 0, 23, '安徽', '2020-02-10', 1, 1, '', 'avatar.png', 1);
INSERT INTO `staff` VALUES (12, '小于', '1007', '567d79fb266c4128333b27a2d817cbd9', '3234244', '15466667777', 0, 22, '安徽蒙城', '2020-02-10', 1, 1, '', 'avatar.png', 3);
INSERT INTO `staff` VALUES (13, '老王', '1008', '567d79fb266c4128333b27a2d817cbd9', '645165654654', '18297568956', 0, 22, '杭州市余杭区', '2020-04-07', 1, 1, '', 'avatar.png', 2);

-- ----------------------------
-- Table structure for ware
-- ----------------------------
DROP TABLE IF EXISTS `ware`;
CREATE TABLE `ware`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `wareName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ware
-- ----------------------------
INSERT INTO `ware` VALUES (1, 'A仓库');
INSERT INTO `ware` VALUES (2, 'B仓库');
INSERT INTO `ware` VALUES (3, 'C仓库');
INSERT INTO `ware` VALUES (4, 'D仓库');
INSERT INTO `ware` VALUES (5, 'E仓库');

SET FOREIGN_KEY_CHECKS = 1;

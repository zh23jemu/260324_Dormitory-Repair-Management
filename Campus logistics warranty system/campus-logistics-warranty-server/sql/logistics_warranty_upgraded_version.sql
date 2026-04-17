/*
 Navicat Premium Dump SQL

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 80020 (8.0.20)
 Source Host           : localhost:3306
 Source Schema         : logistics_warranty_upgraded_version

 Target Server Type    : MySQL
 Target Server Version : 80020 (8.0.20)
 File Encoding         : 65001

 Date: 08/04/2026 20:29:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for collect
-- ----------------------------
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '被收藏项ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = '用户收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of collect
-- ----------------------------
INSERT INTO `collect` VALUES (7, 4, 8, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `collect` VALUES (8, 1, 10, '2026-02-14 20:23:32', '2026-04-08 17:32:06');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` int UNSIGNED NOT NULL COMMENT '关联文章ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '评论用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `like_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数量',
  `parent_id` int UNSIGNED NULL DEFAULT NULL COMMENT '父评论ID（支持回复功能）',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0-正常 1-删除',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article`(`post_id` ASC) USING BTREE,
  INDEX `idx_created`(`created_time` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (15, 1, 201, '非常感谢维修人员及时处理！', 5, NULL, 0, '2023-10-01 10:30:00', '2023-10-01 10:30:00');
INSERT INTO `comment` VALUES (16, 1, 202, '我也遇到同样的问题，谢谢解决了！', 3, NULL, 0, '2023-10-01 11:45:00', '2023-10-01 11:45:00');
INSERT INTO `comment` VALUES (17, 1, 203, '请问还有其他需要注意的地方吗？', 0, 15, 0, '2023-10-01 12:00:00', '2025-05-14 23:33:58');
INSERT INTO `comment` VALUES (18, 2, 204, '这个问题确实存在一段时间了，希望尽快解决。', 4, NULL, 0, '2023-10-02 09:15:00', '2023-10-02 09:15:00');
INSERT INTO `comment` VALUES (19, 2, 205, '同意，这个情况影响了很多人的生活。', 2, 17, 0, '2023-10-02 09:30:00', '2025-05-14 23:33:58');
INSERT INTO `comment` VALUES (20, 3, 206, '电脑运行速度慢可能是软件问题，可以先清理一下垃圾文件。', 7, NULL, 0, '2023-10-03 14:00:00', '2023-10-03 14:00:00');
INSERT INTO `comment` VALUES (21, 3, 207, '是的，我已经试过了，但没有明显改善。', 1, 19, 0, '2023-10-03 14:15:00', '2025-05-14 23:33:58');
INSERT INTO `comment` VALUES (22, 4, 208, '漏水的情况已经严重影响到我们的日常生活，请尽快修复。', 6, 20, 0, '2023-10-04 10:00:00', '2025-05-14 23:33:58');
INSERT INTO `comment` VALUES (23, 5, 209, '空调不制冷确实是比较严重的问题，特别是天气热的时候。', 8, 20, 1, '2023-10-05 11:00:00', '2025-05-14 23:41:19');

-- ----------------------------
-- Table structure for dorm_building
-- ----------------------------
DROP TABLE IF EXISTS `dorm_building`;
CREATE TABLE `dorm_building`  (
  `building_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '楼栋编号，如 A, B1',
  `building_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '楼栋名称，如 男生1号楼',
  `building_type` tinyint NOT NULL DEFAULT 1 COMMENT '类型：1-男生宿舍，2-女生宿舍，3-混合，4-教职工',
  `img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `total_floors` tinyint UNSIGNED NOT NULL COMMENT '总楼层数',
  `manager_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '宿管姓名',
  `manager_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '宿管电话',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用，2-维修中',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`building_id`) USING BTREE,
  INDEX `idx_building_type`(`building_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `dorm_building_chk_1` CHECK (`total_floors` >= 1)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宿舍楼信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dorm_building
-- ----------------------------
INSERT INTO `dorm_building` VALUES ('A', '男生1号楼', 1, NULL, 6, '张伟', '13800138001', 1, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_building` VALUES ('B', '女生1号楼', 2, NULL, 6, '李芳', '13800138002', 1, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_building` VALUES ('C', '研究生混合楼', 3, NULL, 5, '王强', '13800138003', 1, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_building` VALUES ('D', '教职工公寓', 4, NULL, 4, '赵敏', '13800138004', 1, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_building` VALUES ('E', '男生2号楼', 1, 'http://localhost:8080/upload/1767596634449.png', 6, '刘洋', '13800138005', 1, '2026-02-14 20:23:32', '2026-04-08 17:32:06');

-- ----------------------------
-- Table structure for dorm_facility
-- ----------------------------
DROP TABLE IF EXISTS `dorm_facility`;
CREATE TABLE `dorm_facility`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '物品ID',
  `room_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属房间ID（逻辑外键，关联 dorm_room.room_id）',
  `facility_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品名称，如 空调、上下床、书桌',
  `facility_type` tinyint NOT NULL DEFAULT 1 COMMENT '物品类型：1-家具，2-电器，3-卫浴，4-其他',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌（可选）',
  `model_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '型号',
  `purchase_date` date NULL DEFAULT NULL COMMENT '购置日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-损坏，1-正常，2-维修中，3-报废',
  `remarks` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注，如“制冷效果差”',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_facility_type`(`facility_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宿舍物品/设施表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dorm_facility
-- ----------------------------
INSERT INTO `dorm_facility` VALUES (1, 'A-201', '上下床（4套）', 1, '华森', 'HS-BED-4U', '2022-08-01', 1, NULL, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (2, 'A-201', '书桌（4张）', 1, '华森', 'HS-DESK-4', '2022-08-01', 1, NULL, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (3, 'A-201', '空调', 2, '格力', 'KFR-35GW', '2022-08-10', 0, '不制冷，待维修', '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (4, 'A-201', '电风扇', 2, '美的', 'FS40', '2023-06-01', 1, NULL, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (5, 'B-201', '上下床（6套）', 1, '华森', 'HS-BED-6U', '2021-07-15', 1, NULL, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (6, 'B-201', '衣柜（6组）', 1, '定制', NULL, '2021-07-15', 1, NULL, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (7, 'B-201', '热水器', 3, '海尔', 'ES60H', '2021-08-01', 2, '正在维修', '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (8, 'C-102', '单人床', 1, '宜家', 'MALM', '2023-09-01', 1, NULL, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (9, 'C-102', '书桌', 1, '宜家', 'MICKE', '2023-09-01', 1, NULL, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (10, 'C-102', '台灯', 2, '小米', 'MJTD01', '2023-09-05', 1, NULL, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (11, 'D-201', '双人床', 1, '顾家', 'KB-8801', '2020-05-20', 1, NULL, '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (12, 'D-201', '沙发', 1, '顾家', 'SF-2020', '2020-05-20', 3, '已报废，待清理', '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `dorm_facility` VALUES (13, 'D-201', '冰箱', 2, '海尔', 'BCD-216ST', '2020-06-01', 1, NULL, '2026-02-14 20:23:32', '2026-04-08 17:32:06');

-- ----------------------------
-- Table structure for dorm_room
-- ----------------------------
DROP TABLE IF EXISTS `dorm_room`;
CREATE TABLE `dorm_room`  (
  `room_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房间ID，如 A-201',
  `building_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属楼栋编号（逻辑外键，关联 dorm_building.building_id）',
  `floor` tinyint UNSIGNED NOT NULL COMMENT '楼层，如 2',
  `room_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房间号，如 201',
  `max_capacity` tinyint UNSIGNED NOT NULL DEFAULT 4 COMMENT '最大容纳人数',
  `current_occupancy` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '当前入住人数',
  `room_type` tinyint NOT NULL DEFAULT 1 COMMENT '房型：1-四人间，2-六人间，3-双人间，4-单人间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-空闲，1-部分入住，2-已满，3-维修中',
  `remarks` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`room_id`) USING BTREE,
  UNIQUE INDEX `uk_building_floor_room`(`building_id` ASC, `floor` ASC, `room_number` ASC) USING BTREE,
  INDEX `idx_building_id`(`building_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_floor`(`floor` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宿舍房间表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dorm_room
-- ----------------------------
INSERT INTO `dorm_room` VALUES ('A-202', 'A', 2, '202', 4, 4, 1, 2, NULL, '2026-02-14 20:23:32');
INSERT INTO `dorm_room` VALUES ('A-203', 'A', 2, '203', 4, 0, 1, 0, '空调待修', '2026-02-14 20:23:32');
INSERT INTO `dorm_room` VALUES ('A-301', 'A', 3, '301', 4, 2, 1, 1, NULL, '2026-02-14 20:23:32');
INSERT INTO `dorm_room` VALUES ('A-302', 'A', 3, '302', 4, 4, 1, 2, NULL, '2026-02-14 20:23:32');
INSERT INTO `dorm_room` VALUES ('A-401', 'A', 4, '401', 4, 1, 1, 1, '靠窗床位空闲', '2026-02-14 20:23:32');
INSERT INTO `dorm_room` VALUES ('B-201', 'B', 2, '201', 6, 5, 2, 1, NULL, '2026-02-14 20:23:32');
INSERT INTO `dorm_room` VALUES ('B-202', 'B', 2, '202', 6, 6, 2, 2, NULL, '2026-02-14 20:23:32');
INSERT INTO `dorm_room` VALUES ('B-301', 'B', 3, '301', 6, 0, 2, 0, '新装修，未启用', '2026-02-14 20:23:32');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '留言用户ID，关联user表',
  `img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '留言内容',
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '管理员或客服的回复内容',
  `status` int NOT NULL DEFAULT 0 COMMENT '留言状态0待回复1已回复',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '留言时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户留言表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (8, 201, 'http://localhost:8080/upload/1775650751938.jpeg', '请问什么时候可以处理我的报修请求？', '预计今天下午会派维修人员上门服务。', 1, '2026-02-14 20:23:32.000', '2026-04-08 20:23:27.439');
INSERT INTO `message` VALUES (9, 202, 'http://localhost:8080/upload/1775650832146.jpeg', '我发现宿舍的灯泡坏了，请尽快更换。', NULL, 0, '2026-02-14 20:23:32.000', '2026-04-08 20:23:43.763');
INSERT INTO `message` VALUES (10, 203, 'http://localhost:8080/upload/1775650832146.jpeg', '食堂的饭菜质量下降了，希望改进。', '我们会反馈给相关部门，谢谢建议。', 1, '2026-02-14 20:23:32.000', '2026-04-08 20:23:43.756');
INSERT INTO `message` VALUES (11, 204, 'http://localhost:8080/upload/1775650832146.jpeg', '图书馆的座位不够用了，能否增加一些？', NULL, 0, '2026-02-14 20:23:32.000', '2026-04-08 20:23:43.767');
INSERT INTO `message` VALUES (12, 205, 'http://localhost:8080/upload/1775650751938.jpeg', '教学楼的门锁经常失灵，需要维修。', '已记录，将在下周安排维修人员检查。', 1, '2026-02-14 20:23:32.000', '2026-04-08 20:23:27.423');

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '公告状态：1-已发布 2-已过期',
  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (6, '紧急通知：教学楼电梯维护', 'http://localhost:8080/upload/1775650751938.jpeg', '由于安全原因，教学楼电梯将于明天上午10:00至下午2:00进行维护，请提前做好准备。', 1, '2026-02-14 20:23:32.000', '2026-04-08 17:32:06.696');
INSERT INTO `notice` VALUES (7, '校园清洁活动安排', 'http://localhost:8080/upload/1775650869596.jpg', '本周六（10月7日）上午9:00将在全校范围内开展清洁活动，请同学们积极参与。', 1, '2026-02-14 20:23:32.000', '2026-04-08 17:32:06.696');
INSERT INTO `notice` VALUES (8, '图书馆闭馆通知', 'http://localhost:8080/upload/1775650832146.jpeg', '因技术升级，图书馆将于本月20日至25日期间闭馆，请提前借阅所需书籍。', 1, '2026-02-14 20:23:32.000', '2026-04-08 17:32:06.696');
INSERT INTO `notice` VALUES (9, '下学期开学安排', 'http://localhost:8080/upload/1775650883039.jpg', '下学期将于2024年2月18日正式开学，请各位同学按时返校注册。', 1, '2026-02-14 20:23:32.000', '2026-04-08 17:32:06.696');

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` int NULL DEFAULT 0 COMMENT '状态0-待审核1审核通过2下架',
  `author_id` bigint NOT NULL,
  `views` int NULL DEFAULT 0,
  `likes` int NULL DEFAULT 0,
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post
-- ----------------------------
INSERT INTO `post` VALUES (8, '教室投影仪故障', '教室投影仪无法正常工作', 'http://localhost:8080/upload/1775650519337.jpg', '今天早上发现教室的投影仪无法启动，已经尝试重启多次但无果。希望尽快得到维修。', 1, 1, 55, 12, '2025-05-14 23:30:30', '2025-05-14 23:40:58');
INSERT INTO `post` VALUES (9, '宿舍床铺损坏', '宿舍床上的床垫有破损', 'http://localhost:8080/upload/1775650556968.jpeg', '我的宿舍床位上的床垫出现了明显的破损，影响了休息质量。希望能更换一个新的床垫。', 0, 2, 32, 5, '2025-05-14 23:30:30', '2025-05-14 23:40:59');
INSERT INTO `post` VALUES (10, '实验室电脑问题', '实验室的电脑运行缓慢', 'http://localhost:8080/upload/1775128996795.jpg', '实验室的多台电脑运行速度非常慢，导致我们的实验进度受到影响。请检查并解决这个问题。', 1, 3, 88, 19, '2025-05-14 23:30:30', '2025-05-14 23:40:58');
INSERT INTO `post` VALUES (11, '厕所水管漏水', '男厕水管持续漏水', 'http://localhost:8080/upload/1775650577215.jpg', '男厕的一个水管一直在漏水，水已经漫到了地面上，造成了很大的不便。请尽快派人修理。', 2, 4, 21, 3, '2025-05-14 23:30:30', '2025-05-14 23:40:58');
INSERT INTO `post` VALUES (12, '食堂空调不制冷', '食堂空调无法降温', 'http://localhost:8080/upload/1775650594052.jpg', '食堂的空调一直没有开启制冷模式，天气炎热的情况下用餐体验很差。希望能够恢复正常制冷功能。', 1, 1, 61, 12, '2025-05-14 23:30:30', '2025-05-14 23:40:58');
INSERT INTO `post` VALUES (15, '实验室电脑问题', '实验室的电脑运行缓慢', 'http://localhost:8080/upload/1775129008749.jpg', '实验室的多台电脑运行速度非常慢，导致我们的实验进度受到影响。请检查并解决这个问题。', 1, 4, 71, 15, '2025-05-14 23:30:31', '2025-05-14 23:40:59');

-- ----------------------------
-- Table structure for repair_record
-- ----------------------------
DROP TABLE IF EXISTS `repair_record`;
CREATE TABLE `repair_record`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '维修记录ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '报修用户ID（逻辑外键到user表）',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type_id` bigint UNSIGNED NOT NULL COMMENT '维修类型ID（逻辑外键到repair_type表）',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '维修地点',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题描述',
  `img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片地址（可选）',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '待处理' COMMENT '状态：待处理/处理中/已完成/已取消',
  `is_charge` int NOT NULL DEFAULT 0 COMMENT '1收0免',
  `price` int NULL DEFAULT NULL,
  `worker_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '处理人ID（逻辑外键到user表）',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理备注',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '维修记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repair_record
-- ----------------------------
INSERT INTO `repair_record` VALUES (1, 1, '图书馆一楼洗手间', 1, '图书馆一楼洗手间', '18362302136', '水龙头漏水严重，请尽快派人修理', 'http://localhost:8080/upload/1775641208491.jpeg', '处理中', 0, NULL, 3, NULL, '2026-02-14 20:23:32.000', '2026-04-08 17:32:06.713');
INSERT INTO `repair_record` VALUES (2, 2, '书桌抽屉损坏无法拉开', 2, '学生宿舍4号楼507室', '18323202361', '书桌抽屉损坏无法拉开', 'http://localhost:8080/upload/1775641237321.jpeg', '处理中', 0, NULL, 3, '已经更换新的抽屉轨道', '2026-02-14 20:23:32.000', '2026-04-08 17:32:06.713');
INSERT INTO `repair_record` VALUES (3, 3, '教室内的无线网络连接不稳定', 3, '教学楼B区308教室', '15865632021', '教室内的无线网络连接不稳定', 'http://localhost:8080/upload/1775641315729.png', '已完成', 0, NULL, 2, '重启路由器后恢复正常', '2026-02-14 20:23:32.000', '2026-04-08 17:32:06.713');

-- ----------------------------
-- Table structure for repair_record_rating
-- ----------------------------
DROP TABLE IF EXISTS `repair_record_rating`;
CREATE TABLE `repair_record_rating`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评分ID，主键',
  `user_id` bigint NOT NULL COMMENT '评分用户ID，外键关联 users 表',
  `repair_record_id` bigint NOT NULL COMMENT '商品ID，外键关联 goods 表',
  `rating` tinyint NOT NULL COMMENT '评分值，范围1-5',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评论内容',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '维修单评分表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repair_record_rating
-- ----------------------------
INSERT INTO `repair_record_rating` VALUES (1, 1, 1, 5, '服务态度很好，问题解决得很及时！', '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `repair_record_rating` VALUES (2, 2, 1, 4, '问题得到了快速解决，但可以更快些。', '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `repair_record_rating` VALUES (3, 3, 1, 3, '服务还可以，不过等待时间有点长。', '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `repair_record_rating` VALUES (4, 4, 2, 2, '问题还没完全解决，还需要进一步处理。', '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `repair_record_rating` VALUES (5, 1, 2, 1, '完全没有解决问题，希望尽快重新安排维修人员。', '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `repair_record_rating` VALUES (6, 2, 3, 5, '电脑运行速度明显提升，非常满意！', '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `repair_record_rating` VALUES (7, 3, 3, 4, '清理垃圾文件后有所改善，但仍需优化。', '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `repair_record_rating` VALUES (8, 4, 4, 3, '漏水情况已经缓解，但还需继续关注。', '2026-02-14 20:23:32', '2026-04-08 17:32:06');
INSERT INTO `repair_record_rating` VALUES (9, 209, 5, 5, '空调现在可以正常使用了，谢谢维修人员！', '2026-02-14 20:23:32', '2026-04-08 17:32:06');

-- ----------------------------
-- Table structure for repair_type
-- ----------------------------
DROP TABLE IF EXISTS `repair_type`;
CREATE TABLE `repair_type`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '维修类型ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '维修类型名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述信息',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '维修类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repair_type
-- ----------------------------
INSERT INTO `repair_type` VALUES (1, '水电维修', '包括水管漏水、电路故障等', '2026-02-14 20:23:32.000', '2026-04-08 17:32:06.744');
INSERT INTO `repair_type` VALUES (2, '家具维修', '桌椅、门窗、柜子等损坏修复', '2026-02-14 20:23:32.000', '2026-04-08 17:32:06.744');
INSERT INTO `repair_type` VALUES (3, '网络故障', '网线不通、无线信号差等问题', '2026-02-14 20:23:32.000', '2026-04-08 17:32:06.744');
INSERT INTO `repair_type` VALUES (4, '空调维修', '空调无法启动或制冷效果差', '2026-02-14 20:23:32.000', '2026-04-08 17:32:06.744');

-- ----------------------------
-- Table structure for resource
-- ----------------------------
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '资源描述',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件路径',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of resource
-- ----------------------------
INSERT INTO `resource` VALUES (7, '维修手册', '详细说明各类设备的维修步骤和技巧', 'https://example.com/resources/manual.pdf', '2026-02-14 20:23:32', '2026-04-08 17:32:06', 'http://localhost:8080/upload/1775640619620.jpg');
INSERT INTO `resource` VALUES (8, '安全指南', '校园后勤安全操作规程', 'https://example.com/resources/safety_guide.pdf', '2026-02-14 20:23:32', '2026-04-08 17:34:38', 'http://localhost:8080/upload/1775640884910.jpg');
INSERT INTO `resource` VALUES (9, '常见问题解答', '关于报修系统的一些常见问题及解决方法', 'https://example.com/resources/faq.txt', '2026-02-14 20:23:32', '2026-04-08 17:32:51', 'http://localhost:8080/upload/1775640834824.jpg');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户头像',
  `sex` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修理工的标签英文逗号分隔：水电维修、卫浴/下水维修、网络/多媒体设备维修、空调/风扇维修、门窗桌椅维修',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` int NULL DEFAULT 0 COMMENT '使用状态 0.正常 1拉黑',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '张三', '13800001234', 'http://localhost:8080/upload/1775640483986.jpeg', '男', 'test', '123456', 'user', NULL, 'zhangsan@example.com', 0, '2026-02-14 20:23:32.000', '2026-02-14 20:23:32.000');
INSERT INTO `user` VALUES (2, '李四', '13800005678', 'http://localhost:8080/upload/1775640489288.jpeg', '女', 'admin', '123456', 'admin', NULL, 'lisi@example.com', 0, '2026-02-14 20:23:32.000', '2026-02-14 20:23:32.000');
INSERT INTO `user` VALUES (3, '王五', '13800009876', 'http://localhost:8080/upload/1775640516929.jpeg', '男', 'wangwu', '123456', 'worker', '水电维修,空调/风扇维修,测试', 'wangwu@example.com', 0, '2026-02-14 20:23:32.000', '2026-02-14 20:23:32.000');
INSERT INTO `user` VALUES (4, '赵六33', '18364073156', 'http://localhost:8080/upload/1775640497793.jpeg', '男', 'zhaoliu', '123456', 'user', NULL, 'zhaoliu@example.com', 0, '2026-02-14 20:23:32.000', '2026-02-14 20:23:32.000');

SET FOREIGN_KEY_CHECKS = 1;

/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80041
 Source Host           : localhost:3306
 Source Schema         : industrial_software_system

 Target Server Type    : MySQL
 Target Server Version : 80041
 File Encoding         : 65001

 Date: 22/08/2025 22:39:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for file_meta
-- ----------------------------
DROP TABLE IF EXISTS `file_meta`;
CREATE TABLE `file_meta`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件唯一ID，用于API调用',
  `file_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件系统中的唯一标识符 (UUID)，用于防止文件名冲突',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户上传时原始文件名',
  `file_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件在服务器上的物理存储路径',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小 (单位: B)',
  `file_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件的MIME类型 (例如: application/pdf)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文件描述',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `creator_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者用户名 (冗余字段，方便查询)',
  `storage_location` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'LOCAL_DISK' COMMENT '存储位置 (LOCAL_DISK, ALI_OSS等)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `db_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件隶属于的数据库名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `file_uuid`(`file_uuid` ASC) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `idx_file_name`(`file_name` ASC) USING BTREE INVISIBLE,
  INDEX `idx_db_type`(`db_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件元数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_meta
-- ----------------------------
INSERT INTO `file_meta` VALUES (5, '0c7ee539a4cb415fa195dafa5a41b4eb', '测试文件1_07252353.png', 'C:/Users/华为Matebook14/Desktop/FileUpload\\0c7ee539a4cb415fa195dafa5a41b4eb.png', 183551, 'image/png', NULL, 1, '测试用户', 'LOCAL_DISK', '2025-07-25 23:53:19', '2025-07-25 23:53:19', 'simulationResult');
INSERT INTO `file_meta` VALUES (6, '6fbf698b853b44798bdc81bbc9cde719', '测试文件1_07252353.png', 'C:/Users/华为Matebook14/Desktop/FileUpload\\6fbf698b853b44798bdc81bbc9cde719.png', 183551, 'image/png', NULL, 1, '测试用户', 'LOCAL_DISK', '2025-07-25 23:54:06', '2025-07-25 23:54:06', 'simulationResult');
INSERT INTO `file_meta` VALUES (7, '06a3c88032864a648170d68c9bf5ff0d', 'testFile.png', 'C:/Users/华为Matebook14/Desktop/FileUpload\\06a3c88032864a648170d68c9bf5ff0d.png', 74017, 'image/png', NULL, 1, '测试用户', 'LOCAL_DISK', '2025-07-26 00:08:19', '2025-07-26 00:08:19', 'materialConstitutive');
INSERT INTO `file_meta` VALUES (8, '943a0a5bea124af1890350bd82b4edb9', 'testFile.png', 'C:/Users/华为Matebook14/Desktop/FileUpload\\943a0a5bea124af1890350bd82b4edb9.png', 74017, 'image/png', NULL, 1, '测试用户', 'LOCAL_DISK', '2025-07-26 00:10:26', '2025-07-26 00:10:26', 'materialConstitutive');
INSERT INTO `file_meta` VALUES (9, 'ec75a215e89043c9b74c82e00dd76593', 'testFIile01.png', 'C:/Users/华为Matebook14/Desktop/FileUpload\\ec75a215e89043c9b74c82e00dd76593.png', 29309, 'image/png', NULL, 1, '测试用户', 'LOCAL_DISK', '2025-07-28 00:03:51', '2025-07-28 00:03:51', 'simulationResult');
INSERT INTO `file_meta` VALUES (10, '80ca717fcc914546a39ba506cef73756', 'testFIile01.png', 'C:/Users/华为Matebook14/Desktop/FileUpload\\80ca717fcc914546a39ba506cef73756.png', 29309, 'image/png', NULL, 1, '测试用户', 'LOCAL_DISK', '2025-07-28 00:03:58', '2025-07-28 00:03:58', 'simulationResult');
INSERT INTO `file_meta` VALUES (11, '087629f118e64e7da0e35450960492f2', 'testFIile01.png', 'C:/Users/华为Matebook14/Desktop/FileUpload\\087629f118e64e7da0e35450960492f2.png', 29309, 'image/png', NULL, 1, '测试用户', 'LOCAL_DISK', '2025-07-28 00:04:47', '2025-07-28 00:04:47', 'simulationResult');
INSERT INTO `file_meta` VALUES (12, 'a88f92465ddf432c8c8eccb6fc04a440', 'testFIile01.png', 'C:/Users/华为Matebook14/Desktop/FileUpload\\a88f92465ddf432c8c8eccb6fc04a440.png', 29309, 'image/png', NULL, 1, '测试用户', 'LOCAL_DISK', '2025-07-28 00:05:12', '2025-07-28 00:05:12', 'simulationResult');

-- ----------------------------
-- Table structure for internal_force_solution_attributes
-- ----------------------------
DROP TABLE IF EXISTS `internal_force_solution_attributes`;
CREATE TABLE `internal_force_solution_attributes`  (
  `internal_force_solution_id` int NOT NULL AUTO_INCREMENT,
  `internal_force_solution_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `op2_file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `internal_force_solution_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `internal_force_solution_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`internal_force_solution_id`) USING BTREE,
  INDEX `internal_force_solution_name`(`internal_force_solution_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of internal_force_solution_attributes
-- ----------------------------

-- ----------------------------
-- Table structure for load_spectrum_attributes
-- ----------------------------
DROP TABLE IF EXISTS `load_spectrum_attributes`;
CREATE TABLE `load_spectrum_attributes`  (
  `load_spectrum_id` int NOT NULL AUTO_INCREMENT,
  `load_spectrum_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `load_spectrum_file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `load_spectrum_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `load_spectrum_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`load_spectrum_id`) USING BTREE,
  UNIQUE INDEX `load_spectrum_name`(`load_spectrum_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of load_spectrum_attributes
-- ----------------------------

-- ----------------------------
-- Table structure for material_attributes
-- ----------------------------
DROP TABLE IF EXISTS `material_attributes`;
CREATE TABLE `material_attributes`  (
  `material_attributes_id` int NOT NULL AUTO_INCREMENT,
  `density` float NULL DEFAULT NULL,
  `youngs_modulus` float NULL DEFAULT NULL,
  `poisson_ratio` float NULL DEFAULT NULL,
  `yield_strength` float NULL DEFAULT NULL,
  `tensile_strength` float NULL DEFAULT NULL,
  `fracture_toughness` float NULL DEFAULT NULL,
  PRIMARY KEY (`material_attributes_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of material_attributes
-- ----------------------------

-- ----------------------------
-- Table structure for mesh_attributes
-- ----------------------------
DROP TABLE IF EXISTS `mesh_attributes`;
CREATE TABLE `mesh_attributes`  (
  `mesh_attributes_id` int NOT NULL AUTO_INCREMENT,
  `mesh_type` int NULL DEFAULT NULL,
  `node_coordinates` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `material_attributes_id` int NOT NULL,
  PRIMARY KEY (`mesh_attributes_id`) USING BTREE,
  INDEX `material_attributes_id`(`material_attributes_id` ASC) USING BTREE,
  INDEX `mesh_type`(`mesh_type` ASC) USING BTREE,
  CONSTRAINT `mesh_attributes_ibfk_1` FOREIGN KEY (`material_attributes_id`) REFERENCES `material_attributes` (`material_attributes_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mesh_attributes
-- ----------------------------

-- ----------------------------
-- Table structure for mod_base_attributes
-- ----------------------------
DROP TABLE IF EXISTS `mod_base_attributes`;
CREATE TABLE `mod_base_attributes`  (
  `base_id` int NOT NULL AUTO_INCREMENT,
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `device_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `creator` int NOT NULL,
  PRIMARY KEY (`base_id`) USING BTREE,
  INDEX `creator`(`creator` ASC) USING BTREE,
  CONSTRAINT `mod_base_attributes_ibfk_1` FOREIGN KEY (`creator`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mod_base_attributes
-- ----------------------------

-- ----------------------------
-- Table structure for mod_devices
-- ----------------------------
DROP TABLE IF EXISTS `mod_devices`;
CREATE TABLE `mod_devices`  (
  `device_id` int NOT NULL AUTO_INCREMENT,
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `base_id` int NOT NULL,
  `task_id` int NOT NULL,
  PRIMARY KEY (`device_id`) USING BTREE,
  INDEX `base_id`(`base_id` ASC) USING BTREE,
  INDEX `device_type`(`device_type` ASC) USING BTREE,
  INDEX `task_id`(`task_id` ASC) USING BTREE,
  CONSTRAINT `mod_devices_ibfk_1` FOREIGN KEY (`base_id`) REFERENCES `mod_base_attributes` (`base_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `mod_devices_ibfk_2` FOREIGN KEY (`task_id`) REFERENCES `mod_tasks` (`task_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mod_devices
-- ----------------------------

-- ----------------------------
-- Table structure for mod_files
-- ----------------------------
DROP TABLE IF EXISTS `mod_files`;
CREATE TABLE `mod_files`  (
  `file_id` int NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `file_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `stage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `file_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `creation_time` datetime NOT NULL,
  `modification_time` datetime NULL DEFAULT NULL,
  `project_id` int NOT NULL,
  PRIMARY KEY (`file_id`) USING BTREE,
  INDEX `project_id`(`project_id` ASC) USING BTREE,
  INDEX `file_name`(`file_name` ASC) USING BTREE,
  CONSTRAINT `mod_files_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `mod_projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mod_files
-- ----------------------------

-- ----------------------------
-- Table structure for mod_projects
-- ----------------------------
DROP TABLE IF EXISTS `mod_projects`;
CREATE TABLE `mod_projects`  (
  `project_id` int NOT NULL AUTO_INCREMENT,
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `creator` int NOT NULL,
  `creation_time` datetime NOT NULL,
  `organization_id` int NULL DEFAULT NULL,
  `project_status` int NULL DEFAULT 0 COMMENT '项目状态：0-共享项目，1-私人项目',
  PRIMARY KEY (`project_id`) USING BTREE,
  INDEX `creator`(`creator` ASC) USING BTREE,
  INDEX `project_name`(`project_name` ASC) USING BTREE,
  INDEX `project_id`(`project_id` ASC) USING BTREE,
  INDEX `organization_id`(`organization_id` ASC) USING BTREE,
  CONSTRAINT `mod_projects_ibfk_1` FOREIGN KEY (`creator`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `mod_projects_ibfk_2` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`org_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mod_projects
-- ----------------------------

-- ----------------------------
-- Table structure for mod_requirements
-- ----------------------------
DROP TABLE IF EXISTS `mod_requirements`;
CREATE TABLE `mod_requirements`  (
  `requirement_id` int NOT NULL AUTO_INCREMENT,
  `requirement_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `requirement_parameters` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `task_id` int NOT NULL,
  PRIMARY KEY (`requirement_id`) USING BTREE,
  INDEX `requirement_type`(`requirement_type` ASC) USING BTREE,
  INDEX `task_id`(`task_id` ASC) USING BTREE,
  CONSTRAINT `mod_requirements_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `mod_tasks` (`task_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mod_requirements
-- ----------------------------

-- ----------------------------
-- Table structure for mod_tasks
-- ----------------------------
DROP TABLE IF EXISTS `mod_tasks`;
CREATE TABLE `mod_tasks`  (
  `task_id` int NOT NULL AUTO_INCREMENT,
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `creator` int NOT NULL,
  `creation_time` datetime NOT NULL,
  `project_id` int NOT NULL,
  `simulation_stage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '仿真阶段（前处理、后处理、求解器）',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务类型（多体、结构、冲击、通用后处理）',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '未启动' COMMENT '任务状态（未启动、仿真中、暂停中）',
  `compute_resource` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '计算类型（GPU等）',
  PRIMARY KEY (`task_id`) USING BTREE,
  INDEX `project_id`(`project_id` ASC) USING BTREE,
  INDEX `creator`(`creator` ASC) USING BTREE,
  CONSTRAINT `mod_tasks_ibfk_1` FOREIGN KEY (`creator`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `mod_tasks_ibfk_2` FOREIGN KEY (`project_id`) REFERENCES `mod_projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mod_tasks
-- ----------------------------

-- ----------------------------
-- Table structure for mod_user_authorizations
-- ----------------------------
DROP TABLE IF EXISTS `mod_user_authorizations`;
CREATE TABLE `mod_user_authorizations`  (
  `authorization_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `permitted_tables` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `create_permission` tinyint(1) NULL DEFAULT NULL,
  `read_permission` tinyint(1) NULL DEFAULT NULL,
  `update_permission` tinyint(1) NULL DEFAULT NULL,
  `delete_permission` tinyint(1) NULL DEFAULT NULL,
  PRIMARY KEY (`authorization_id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `mod_user_authorizations_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mod_user_authorizations
-- ----------------------------

-- ----------------------------
-- Table structure for mod_users
-- ----------------------------
DROP TABLE IF EXISTS `mod_users`;
CREATE TABLE `mod_users`  (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `permission` int NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `version` int NULL DEFAULT 0,
  `task_permission` int NULL DEFAULT 1 COMMENT '任务权限：0-个人权限，1-组织权限',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `username_2`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mod_users
-- ----------------------------
INSERT INTO `mod_users` VALUES (20, '张三', '$2a$10$dqG1yMAPgUfuhHcmGFnw3e9EjXBaaFN6bxeIypzf9dLZTkpUBta2i', 0, '13726139710', 0, 1);

-- ----------------------------
-- Table structure for operator_attributes
-- ----------------------------
DROP TABLE IF EXISTS `operator_attributes`;
CREATE TABLE `operator_attributes`  (
  `operator_id` int NOT NULL AUTO_INCREMENT,
  `operator_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `algorithm_realize` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `calculate_object` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `operator_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `application_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `material_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `algorithm_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`operator_id`) USING BTREE,
  INDEX `operator_name`(`operator_name` ASC) USING BTREE,
  INDEX `calculate_object`(`calculate_object`(255) ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of operator_attributes
-- ----------------------------

-- ----------------------------
-- Table structure for organization
-- ----------------------------
DROP TABLE IF EXISTS `organization`;
CREATE TABLE `organization`  (
  `org_id` int NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `org_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织名称',
  `create_user_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`org_id`) USING BTREE,
  INDEX `create_user_id`(`create_user_id` ASC) USING BTREE,
  CONSTRAINT `organization_ibfk_1` FOREIGN KEY (`create_user_id`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '组织表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of organization
-- ----------------------------

-- ----------------------------
-- Table structure for rigid_body_attributes
-- ----------------------------
DROP TABLE IF EXISTS `rigid_body_attributes`;
CREATE TABLE `rigid_body_attributes`  (
  `rigid_body_attributes_id` int NOT NULL AUTO_INCREMENT,
  `rigid_body_shape` int NULL DEFAULT NULL,
  `center_of_mass` float NULL DEFAULT NULL,
  `connection_information` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `material_attributes_id` int NOT NULL,
  `move` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`rigid_body_attributes_id`) USING BTREE,
  INDEX `material_attributes_id`(`material_attributes_id` ASC) USING BTREE,
  INDEX `center_of_mass`(`center_of_mass` ASC) USING BTREE,
  CONSTRAINT `rigid_body_attributes_ibfk_1` FOREIGN KEY (`material_attributes_id`) REFERENCES `material_attributes` (`material_attributes_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of rigid_body_attributes
-- ----------------------------

-- ----------------------------
-- Table structure for user_organization
-- ----------------------------
DROP TABLE IF EXISTS `user_organization`;
CREATE TABLE `user_organization`  (
  `user_id` int NOT NULL COMMENT '用户ID',
  `org_id` int NOT NULL COMMENT '组织ID',
  PRIMARY KEY (`user_id`, `org_id`) USING BTREE,
  INDEX `org_id`(`org_id` ASC) USING BTREE,
  CONSTRAINT `user_organization_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_organization_ibfk_2` FOREIGN KEY (`org_id`) REFERENCES `organization` (`org_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户组织关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_organization
-- ----------------------------
INSERT INTO `user_organization` VALUES (1, 1);
INSERT INTO `user_organization` VALUES (2, 1);
INSERT INTO `user_organization` VALUES (5, 1);
INSERT INTO `user_organization` VALUES (19, 1);

-- ----------------------------
-- View structure for device_task_view
-- ----------------------------
DROP VIEW IF EXISTS `device_task_view`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `device_task_view` AS select 1 AS `base_id`,1 AS `device_name`,1 AS `device_description`,1 AS `task_name`;

SET FOREIGN_KEY_CHECKS = 1;

-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: industrial_software
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Temporary view structure for view `device_task_view`
--

DROP TABLE IF EXISTS `device_task_view`;
/*!50001 DROP VIEW IF EXISTS `device_task_view`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `device_task_view` AS SELECT 
 1 AS `base_id`,
 1 AS `device_name`,
 1 AS `device_description`,
 1 AS `task_name`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `file_meta`
--

DROP TABLE IF EXISTS `file_meta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_meta` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件唯一ID，用于API调用',
  `file_uuid` varchar(255) NOT NULL COMMENT '文件系统中的唯一标识符 (UUID)，用于防止文件名冲突',
  `file_name` varchar(255) NOT NULL COMMENT '用户上传时原始文件名',
  `file_path` varchar(512) NOT NULL COMMENT '文件在服务器上的物理存储路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小 (单位: B)',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件的MIME类型 (例如: application/pdf)',
  `description` text COMMENT '文件描述',
  `creator_id` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `creator_name` varchar(255) DEFAULT NULL COMMENT '创建者用户名 (冗余字段，方便查询)',
  `storage_location` varchar(50) DEFAULT 'LOCAL_DISK' COMMENT '存储位置 (LOCAL_DISK, ALI_OSS等)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `db_type` varchar(30) DEFAULT NULL COMMENT '文件隶属于的数据库名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `file_uuid` (`file_uuid`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_file_name` (`file_name`) /*!80000 INVISIBLE */,
  KEY `idx_db_type` (`db_type`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件元数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `internal_force_solution_attributes`
--

DROP TABLE IF EXISTS `internal_force_solution_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `internal_force_solution_attributes` (
  `internal_force_solution_id` int NOT NULL AUTO_INCREMENT,
  `internal_force_solution_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `op2_file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `internal_force_solution_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `internal_force_solution_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`internal_force_solution_id`) USING BTREE,
  KEY `internal_force_solution_name` (`internal_force_solution_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `load_spectrum_attributes`
--

DROP TABLE IF EXISTS `load_spectrum_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `load_spectrum_attributes` (
  `load_spectrum_id` int NOT NULL AUTO_INCREMENT,
  `load_spectrum_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `load_spectrum_file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `load_spectrum_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `load_spectrum_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`load_spectrum_id`) USING BTREE,
  UNIQUE KEY `load_spectrum_name` (`load_spectrum_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `material_attributes`
--

DROP TABLE IF EXISTS `material_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_attributes` (
  `material_attributes_id` int NOT NULL AUTO_INCREMENT,
  `density` float DEFAULT NULL,
  `youngs_modulus` float DEFAULT NULL,
  `poisson_ratio` float DEFAULT NULL,
  `yield_strength` float DEFAULT NULL,
  `tensile_strength` float DEFAULT NULL,
  `fracture_toughness` float DEFAULT NULL,
  PRIMARY KEY (`material_attributes_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mesh_attributes`
--

DROP TABLE IF EXISTS `mesh_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mesh_attributes` (
  `mesh_attributes_id` int NOT NULL AUTO_INCREMENT,
  `mesh_type` int DEFAULT NULL,
  `node_coordinates` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `material_attributes_id` int NOT NULL,
  PRIMARY KEY (`mesh_attributes_id`) USING BTREE,
  KEY `material_attributes_id` (`material_attributes_id`) USING BTREE,
  KEY `mesh_type` (`mesh_type`) USING BTREE,
  CONSTRAINT `mesh_attributes_ibfk_1` FOREIGN KEY (`material_attributes_id`) REFERENCES `material_attributes` (`material_attributes_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mod_base_attributes`
--

DROP TABLE IF EXISTS `mod_base_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mod_base_attributes` (
  `base_id` int NOT NULL AUTO_INCREMENT,
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `device_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `creator` int NOT NULL,
  PRIMARY KEY (`base_id`) USING BTREE,
  KEY `creator` (`creator`) USING BTREE,
  CONSTRAINT `mod_base_attributes_ibfk_1` FOREIGN KEY (`creator`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mod_devices`
--

DROP TABLE IF EXISTS `mod_devices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mod_devices` (
  `device_id` int NOT NULL AUTO_INCREMENT,
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `base_id` int NOT NULL,
  `task_id` int NOT NULL,
  PRIMARY KEY (`device_id`) USING BTREE,
  KEY `base_id` (`base_id`) USING BTREE,
  KEY `device_type` (`device_type`) USING BTREE,
  KEY `task_id` (`task_id`) USING BTREE,
  CONSTRAINT `mod_devices_ibfk_1` FOREIGN KEY (`base_id`) REFERENCES `mod_base_attributes` (`base_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `mod_devices_ibfk_2` FOREIGN KEY (`task_id`) REFERENCES `mod_tasks` (`task_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mod_files`
--

DROP TABLE IF EXISTS `mod_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mod_files` (
  `file_id` int NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `file_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `stage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `file_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `creation_time` datetime NOT NULL,
  `modification_time` datetime DEFAULT NULL,
  `project_id` int NOT NULL,
  PRIMARY KEY (`file_id`) USING BTREE,
  KEY `project_id` (`project_id`) USING BTREE,
  KEY `file_name` (`file_name`) USING BTREE,
  CONSTRAINT `mod_files_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `mod_projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mod_projects`
--

DROP TABLE IF EXISTS `mod_projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mod_projects` (
  `project_id` int NOT NULL AUTO_INCREMENT,
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `creator` int NOT NULL,
  `creation_time` datetime NOT NULL,
  `organization_id` int DEFAULT NULL,
  `project_status` int DEFAULT '0' COMMENT '项目状态：0-共享项目，1-私人项目',
  PRIMARY KEY (`project_id`) USING BTREE,
  KEY `creator` (`creator`) USING BTREE,
  KEY `project_name` (`project_name`) USING BTREE,
  KEY `project_id` (`project_id`) USING BTREE,
  KEY `organization_id` (`organization_id`) USING BTREE,
  CONSTRAINT `mod_projects_ibfk_1` FOREIGN KEY (`creator`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `mod_projects_ibfk_2` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`org_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mod_requirements`
--

DROP TABLE IF EXISTS `mod_requirements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mod_requirements` (
  `requirement_id` int NOT NULL AUTO_INCREMENT,
  `requirement_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `requirement_parameters` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `task_id` int NOT NULL,
  PRIMARY KEY (`requirement_id`) USING BTREE,
  KEY `requirement_type` (`requirement_type`) USING BTREE,
  KEY `task_id` (`task_id`) USING BTREE,
  CONSTRAINT `mod_requirements_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `mod_tasks` (`task_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mod_tasks`
--

DROP TABLE IF EXISTS `mod_tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mod_tasks` (
  `task_id` int NOT NULL AUTO_INCREMENT,
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `creator` int NOT NULL,
  `creation_time` datetime NOT NULL,
  `project_id` int NOT NULL,
  `simulation_stage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '仿真阶段（前处理、后处理、求解器）',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务类型（多体、结构、冲击、通用后处理）',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '未启动' COMMENT '任务状态（未启动、仿真中、暂停中）',
  PRIMARY KEY (`task_id`) USING BTREE,
  KEY `project_id` (`project_id`) USING BTREE,
  KEY `creator` (`creator`) USING BTREE,
  CONSTRAINT `mod_tasks_ibfk_1` FOREIGN KEY (`creator`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `mod_tasks_ibfk_2` FOREIGN KEY (`project_id`) REFERENCES `mod_projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mod_user_authorizations`
--

DROP TABLE IF EXISTS `mod_user_authorizations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mod_user_authorizations` (
  `authorization_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `permitted_tables` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `create_permission` tinyint(1) DEFAULT NULL,
  `read_permission` tinyint(1) DEFAULT NULL,
  `update_permission` tinyint(1) DEFAULT NULL,
  `delete_permission` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`authorization_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `mod_user_authorizations_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mod_users`
--

DROP TABLE IF EXISTS `mod_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mod_users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `permission` int NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `version` int DEFAULT '0',
  `task_permission` int DEFAULT '1' COMMENT '任务权限：0-个人权限，1-组织权限',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE,
  KEY `username_2` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `operator_attributes`
--

DROP TABLE IF EXISTS `operator_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operator_attributes` (
  `operator_id` int NOT NULL AUTO_INCREMENT,
  `operator_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `algorithm_realize` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `calculate_object` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `operator_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `application_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `material_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `algorithm_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`operator_id`) USING BTREE,
  KEY `operator_name` (`operator_name`) USING BTREE,
  KEY `calculate_object` (`calculate_object`(255)) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organization`
--

DROP TABLE IF EXISTS `organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organization` (
  `org_id` int NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `org_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织名称',
  `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`org_id`) USING BTREE,
  KEY `create_user_id` (`create_user_id`) USING BTREE,
  CONSTRAINT `organization_ibfk_1` FOREIGN KEY (`create_user_id`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='组织表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Final view structure for view `device_task_view`
--

/*!50001 DROP VIEW IF EXISTS `device_task_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `device_task_view` AS select 1 AS `base_id`,1 AS `device_name`,1 AS `device_description`,1 AS `task_name` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-24 23:10:00

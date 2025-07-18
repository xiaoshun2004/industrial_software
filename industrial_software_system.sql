-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: industrial_software_system
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
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
-- Dumping data for table `internal_force_solution_attributes`
--

/*!40000 ALTER TABLE `internal_force_solution_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `internal_force_solution_attributes` ENABLE KEYS */;

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
-- Dumping data for table `load_spectrum_attributes`
--

/*!40000 ALTER TABLE `load_spectrum_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `load_spectrum_attributes` ENABLE KEYS */;

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
-- Dumping data for table `material_attributes`
--

/*!40000 ALTER TABLE `material_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `material_attributes` ENABLE KEYS */;

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
-- Dumping data for table `mesh_attributes`
--

/*!40000 ALTER TABLE `mesh_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `mesh_attributes` ENABLE KEYS */;

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
-- Dumping data for table `mod_base_attributes`
--

/*!40000 ALTER TABLE `mod_base_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_base_attributes` ENABLE KEYS */;

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
-- Dumping data for table `mod_devices`
--

/*!40000 ALTER TABLE `mod_devices` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_devices` ENABLE KEYS */;

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
-- Dumping data for table `mod_files`
--

/*!40000 ALTER TABLE `mod_files` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_files` ENABLE KEYS */;

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
-- Dumping data for table `mod_projects`
--

/*!40000 ALTER TABLE `mod_projects` DISABLE KEYS */;
INSERT INTO `mod_projects` VALUES (1,'汽车发动机仿真项目',18,'2025-05-27 14:00:00',1,0),(2,'桥梁结构安全分析',9,'2025-05-27 14:15:00',2,1),(3,'航空发动机叶片优化',2,'2025-05-27 14:30:00',1,0),(4,'高速列车车体仿真',5,'2025-05-27 14:45:00',2,0),(5,'私人研究项目A',11,'2025-05-27 15:00:00',1,1),(7,'ghuighhgui',19,'2025-06-06 16:30:22',1,0),(8,'delete projectStage',19,'2025-06-06 16:34:36',1,0),(9,'DeleteprojectStage',19,'2025-06-06 16:34:53',1,0),(13,'1111',19,'2025-06-06 17:37:12',1,0),(14,'org_id',19,'2025-06-06 17:39:10',1,0),(15,'org_id1234312',19,'2025-06-06 17:40:37',1,0),(16,'org__ididid',19,'2025-06-06 17:41:03',1,1);
/*!40000 ALTER TABLE `mod_projects` ENABLE KEYS */;

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
-- Dumping data for table `mod_requirements`
--

/*!40000 ALTER TABLE `mod_requirements` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_requirements` ENABLE KEYS */;

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
-- Dumping data for table `mod_tasks`
--

/*!40000 ALTER TABLE `mod_tasks` DISABLE KEYS */;
INSERT INTO `mod_tasks` VALUES (1,'发动机燃烧室仿真',18,'2025-05-27 14:05:00',1,'前处理','结构','仿真中'),(2,'气缸压力分析',2,'2025-05-27 14:20:00',1,'求解器','多体','未启动'),(3,'发动机振动分析',18,'2025-05-27 14:35:00',1,'后处理','通用后处理','暂停中'),(4,'桥梁载荷分析',9,'2025-05-27 14:50:00',2,'前处理','结构','仿真中'),(5,'桥梁动力学仿真',9,'2025-05-27 15:05:00',2,'求解器','结构','未启动'),(6,'叶片气动分析',2,'2025-05-27 15:20:00',3,'前处理','结构','仿真中'),(7,'叶片疲劳分析',5,'2025-05-27 15:35:00',3,'求解器','冲击','未启动'),(8,'车体碰撞仿真',5,'2025-05-27 15:50:00',4,'前处理','冲击','仿真中'),(9,'车体结构优化',11,'2025-05-27 16:05:00',4,'后处理','通用后处理','暂停中'),(10,'私人任务测试',11,'2025-05-27 16:20:00',5,'前处理','多体','未启动');
/*!40000 ALTER TABLE `mod_tasks` ENABLE KEYS */;

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
-- Dumping data for table `mod_user_authorizations`
--

/*!40000 ALTER TABLE `mod_user_authorizations` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_user_authorizations` ENABLE KEYS */;

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
-- Dumping data for table `mod_users`
--

/*!40000 ALTER TABLE `mod_users` DISABLE KEYS */;
INSERT INTO `mod_users` VALUES (1,'dsagf','$2a$10$Qu5H.oqfbsV/pbhLi2mA/.JZrF5ALuCtetNUVcngbXvtubevCPm.q',1,'13812345678',4,1),(2,'testUser','$2a$10$VFgTGglGVo3DhL4Y98lgJuKgOpHgcf65JB20FIgVZsTErHxhuVg0u',1,'13812345678',0,1),(5,'testUser01','$2a$10$VFgTGglGVo3DhL4Y98lgJuKgOpHgcf65JB20FIgVZsTErHxhuVg0u',1,'16234567890',0,1),(6,'testUser02','$2a$10$8va8TCT0prn.gBvgx4VabegARg3RRvWGIAqIRUWpWzWAz75u.rqQW',1,'17512345678',0,1),(7,'USER250506','$2a$10$ItLh84Q2zTzSyaYyPsdn9u7Y7km4ObLtrjZCvjtNOUodItrByryia',1,'19876543211',0,1),(9,'maycreate','$2a$10$WaVMUf6WT2Lphz.Awk4CH.0jrFHGBhzbqhdplj0g8aVHc.FU8yUiW',1,'12345678900',0,0),(11,'tomson','$2a$10$JVklcKH3ccI0McXjx6GdT.m/QFuIbSdZGzOLCeku9kiGbl.lv6hTq',0,'12345678920',0,1),(18,'sosofit','$2a$10$MnhwmfzN7.BLLYSpYhRqWO24v4ea9S7a8yWIK.f9GKiOeLIKawIcW',1,'12345678900',0,1),(19,'张三','$2a$10$TOgB6SZ8gIux/.FMoVEuQ.nMHDCFZqinj7i6cFKzNaObTgMPlShF2',1,'13388882250',0,1);
/*!40000 ALTER TABLE `mod_users` ENABLE KEYS */;

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
-- Dumping data for table `operator_attributes`
--

/*!40000 ALTER TABLE `operator_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `operator_attributes` ENABLE KEYS */;

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
-- Dumping data for table `organization`
--

/*!40000 ALTER TABLE `organization` DISABLE KEYS */;
INSERT INTO `organization` VALUES (1,'研发部门',18,'2025-05-27 13:37:45'),(2,'研发采购部门',9,'2025-05-27 13:44:37');
/*!40000 ALTER TABLE `organization` ENABLE KEYS */;

--
-- Temporary view structure for view `project_details_view`
--

DROP TABLE IF EXISTS `project_details_view`;

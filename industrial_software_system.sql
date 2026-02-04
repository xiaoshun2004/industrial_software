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
-- Table structure for table `components`
--

DROP TABLE IF EXISTS `components`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `components` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `dynamics_direction` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `module_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `resource_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `components`
--

LOCK TABLES `components` WRITE;
/*!40000 ALTER TABLE `components` DISABLE KEYS */;
INSERT INTO `components` VALUES (1,'冲击前处理程序','1.0','2.5MB','这是冲击前处理程序','冲击','前处理','CPU','https://www.python.org/ftp/python/pymanager/python-manager-25.2.msi'),(2,'CPU冲击求解器程序','1.0','2.6MB','这是CPU冲击求解器','冲击','求解器','CPU','https://www.python.org/ftp/python/pymanager/python-manager-25.2.msi'),(3,'GPU冲击求解器程序','1.0','2.7MB','这是GPU冲击求解器','冲击','求解器','GPU','https://www.python.org/ftp/python/pymanager/python-manager-25.2.msi'),(4,'结构前处理程序','1.0','2.8MB','这是结构前处理程序','结构','前处理','CPU','https://www.python.org/ftp/python/pymanager/python-manager-25.2.msi'),(5,'结构求解器程序','1.0','2.9MB','这是结构求解器','结构','求解器','CPU','https://www.python.org/ftp/python/pymanager/python-manager-25.2.msi'),(6,'多体前处理程序','1.0','3.0MB','这是多体前处理程序','多体','前处理','CPU','https://www.python.org/ftp/python/pymanager/python-manager-25.2.msi'),(7,'多体求解器程序','1.0','3.1MB','这是多体求解器','多体','求解器','CPU','https://www.python.org/ftp/python/pymanager/python-manager-25.2.msi'),(8,'通用后处理程序','1.0','3.2MB','这是通用后处理程序','通用','后处理','CPU','https://www.python.org/ftp/python/pymanager/python-manager-25.2.msi');
/*!40000 ALTER TABLE `components` ENABLE KEYS */;
UNLOCK TABLES;

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
  `file_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件系统中的唯一标识符 (UUID)，用于防止文件名冲突',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户上传时原始文件名',
  `file_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件在服务器上的物理存储路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小 (单位: B)',
  `file_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件的MIME类型 (例如: application/pdf)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '文件描述',
  `creator_id` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `creator_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者用户名 (冗余字段，方便查询)',
  `storage_location` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'LOCAL_DISK' COMMENT '存储位置 (LOCAL_DISK, ALI_OSS等)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `db_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件隶属于的数据库名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `file_uuid` (`file_uuid`) USING BTREE,
  KEY `idx_creator_id` (`creator_id`) USING BTREE,
  KEY `idx_file_name` (`file_name`) USING BTREE /*!80000 INVISIBLE */,
  KEY `idx_db_type` (`db_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='文件元数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_meta`
--

LOCK TABLES `file_meta` WRITE;
/*!40000 ALTER TABLE `file_meta` DISABLE KEYS */;
INSERT INTO `file_meta` VALUES (5,'0c7ee539a4cb415fa195dafa5a41b4eb','测试文件1_07252353.png','C:/Users/华为Matebook14/Desktop/FileUpload\\0c7ee539a4cb415fa195dafa5a41b4eb.png',183551,'image/png',NULL,1,'测试用户','LOCAL_DISK','2025-07-25 23:53:19','2025-07-25 23:53:19','simulationResult'),(6,'6fbf698b853b44798bdc81bbc9cde719','测试文件1_07252353.png','C:/Users/华为Matebook14/Desktop/FileUpload\\6fbf698b853b44798bdc81bbc9cde719.png',183551,'image/png',NULL,1,'测试用户','LOCAL_DISK','2025-07-25 23:54:06','2025-07-25 23:54:06','simulationResult'),(7,'06a3c88032864a648170d68c9bf5ff0d','testFile.png','C:/Users/华为Matebook14/Desktop/FileUpload\\06a3c88032864a648170d68c9bf5ff0d.png',74017,'image/png',NULL,1,'测试用户','LOCAL_DISK','2025-07-26 00:08:19','2025-07-26 00:08:19','materialConstitutive'),(8,'943a0a5bea124af1890350bd82b4edb9','testFile.png','C:/Users/华为Matebook14/Desktop/FileUpload\\943a0a5bea124af1890350bd82b4edb9.png',74017,'image/png',NULL,1,'测试用户','LOCAL_DISK','2025-07-26 00:10:26','2025-07-26 00:10:26','materialConstitutive'),(9,'ec75a215e89043c9b74c82e00dd76593','testFIile01.png','C:/Users/华为Matebook14/Desktop/FileUpload\\ec75a215e89043c9b74c82e00dd76593.png',29309,'image/png',NULL,1,'测试用户','LOCAL_DISK','2025-07-28 00:03:51','2025-07-28 00:03:51','simulationResult'),(10,'80ca717fcc914546a39ba506cef73756','testFIile01.png','C:/Users/华为Matebook14/Desktop/FileUpload\\80ca717fcc914546a39ba506cef73756.png',29309,'image/png',NULL,1,'测试用户','LOCAL_DISK','2025-07-28 00:03:58','2025-07-28 00:03:58','simulationResult'),(11,'087629f118e64e7da0e35450960492f2','testFIile01.png','C:/Users/华为Matebook14/Desktop/FileUpload\\087629f118e64e7da0e35450960492f2.png',29309,'image/png',NULL,1,'测试用户','LOCAL_DISK','2025-07-28 00:04:47','2025-07-28 00:04:47','simulationResult'),(12,'a88f92465ddf432c8c8eccb6fc04a440','testFIile01.png','C:/Users/华为Matebook14/Desktop/FileUpload\\a88f92465ddf432c8c8eccb6fc04a440.png',29309,'image/png',NULL,1,'测试用户','LOCAL_DISK','2025-07-28 00:05:12','2025-07-28 00:05:12','simulationResult');
/*!40000 ALTER TABLE `file_meta` ENABLE KEYS */;
UNLOCK TABLES;

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

LOCK TABLES `internal_force_solution_attributes` WRITE;
/*!40000 ALTER TABLE `internal_force_solution_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `internal_force_solution_attributes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `license_apply`
--

DROP TABLE IF EXISTS `license_apply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `license_apply` (
  `apply_id` varchar(255) NOT NULL COMMENT '证书申请编号',
  `mac_address` varchar(32) NOT NULL COMMENT '设备的Mac地址',
  `status` varchar(32) NOT NULL COMMENT '证书的申请状态',
  `license_path` varchar(255) DEFAULT NULL COMMENT '证书返回地址',
  `module_id` varchar(32) NOT NULL COMMENT '模块的名称（冲击、多体、结构）',
  `category_id` varchar(32) NOT NULL COMMENT '模块功能性标识（前后处理器、求解器）',
  `valid_from` datetime NOT NULL COMMENT '证书的开始时间',
  `valid_to` datetime NOT NULL COMMENT '证书到期时间',
  `usage_count` int DEFAULT NULL COMMENT '证书可使用次数',
  `created_at` datetime NOT NULL COMMENT '申请创建时间',
  `user_name` varchar(255) NOT NULL COMMENT '申请人的名称',
  `customer_name` varchar(255) DEFAULT NULL COMMENT '客户名称（公司）',
  `license_no` varchar(63) DEFAULT NULL COMMENT '证书编号',
  PRIMARY KEY (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='证书申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `license_apply`
--

LOCK TABLES `license_apply` WRITE;
/*!40000 ALTER TABLE `license_apply` DISABLE KEYS */;
INSERT INTO `license_apply` VALUES ('20190978549813944320010','3C:52:82:1A:9F:01','PENDING',NULL,'pre-impact','pre','2024-07-01 00:00:00','2025-07-02 00:00:00',120,'2026-02-05 01:16:56','张三','中国广电',NULL);
/*!40000 ALTER TABLE `license_apply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `license_result_info`
--

DROP TABLE IF EXISTS `license_result_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `license_result_info` (
  `license_id` int NOT NULL AUTO_INCREMENT COMMENT '唯一证书生成编号',
  `license_name` varchar(255) DEFAULT NULL COMMENT '生成证书的名称',
  `tool_type` varchar(20) NOT NULL COMMENT '证书授权的工具类型（求解器/前处理器/后处理器）',
  `license_path` varchar(255) NOT NULL COMMENT '.lic文件的存储路径',
  `user_id` int NOT NULL COMMENT '证书归属的用户编号',
  `create_time` datetime NOT NULL COMMENT '证书生成的时间',
  `expire_time` datetime NOT NULL COMMENT '证书过期时间',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '证书的当前状态',
  PRIMARY KEY (`license_id`),
  KEY `license_result_mod_users_user_id_fk` (`user_id`),
  CONSTRAINT `license_result_mod_users_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `mod_users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='存储证书生成信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `license_result_info`
--

LOCK TABLES `license_result_info` WRITE;
/*!40000 ALTER TABLE `license_result_info` DISABLE KEYS */;
INSERT INTO `license_result_info` VALUES (1,'structure-1768838856214.lic','Structure','src\\main\\resources\\output\\structure-1768838856214.lic',20,'2026-01-20 00:07:36','2026-02-19 00:07:36',1);
/*!40000 ALTER TABLE `license_result_info` ENABLE KEYS */;
UNLOCK TABLES;

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

LOCK TABLES `load_spectrum_attributes` WRITE;
/*!40000 ALTER TABLE `load_spectrum_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `load_spectrum_attributes` ENABLE KEYS */;
UNLOCK TABLES;

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

LOCK TABLES `material_attributes` WRITE;
/*!40000 ALTER TABLE `material_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `material_attributes` ENABLE KEYS */;
UNLOCK TABLES;

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

LOCK TABLES `mesh_attributes` WRITE;
/*!40000 ALTER TABLE `mesh_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `mesh_attributes` ENABLE KEYS */;
UNLOCK TABLES;

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

LOCK TABLES `mod_base_attributes` WRITE;
/*!40000 ALTER TABLE `mod_base_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_base_attributes` ENABLE KEYS */;
UNLOCK TABLES;

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

LOCK TABLES `mod_devices` WRITE;
/*!40000 ALTER TABLE `mod_devices` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_devices` ENABLE KEYS */;
UNLOCK TABLES;

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

LOCK TABLES `mod_files` WRITE;
/*!40000 ALTER TABLE `mod_files` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_files` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mod_projects`
--

LOCK TABLES `mod_projects` WRITE;
/*!40000 ALTER TABLE `mod_projects` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_projects` ENABLE KEYS */;
UNLOCK TABLES;

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

LOCK TABLES `mod_requirements` WRITE;
/*!40000 ALTER TABLE `mod_requirements` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_requirements` ENABLE KEYS */;
UNLOCK TABLES;

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
  `compute_resource` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '计算类型（GPU等）',
  PRIMARY KEY (`task_id`) USING BTREE,
  KEY `project_id` (`project_id`) USING BTREE,
  KEY `creator` (`creator`) USING BTREE,
  CONSTRAINT `mod_tasks_ibfk_1` FOREIGN KEY (`creator`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `mod_tasks_ibfk_2` FOREIGN KEY (`project_id`) REFERENCES `mod_projects` (`project_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mod_tasks`
--

LOCK TABLES `mod_tasks` WRITE;
/*!40000 ALTER TABLE `mod_tasks` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_tasks` ENABLE KEYS */;
UNLOCK TABLES;

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

LOCK TABLES `mod_user_authorizations` WRITE;
/*!40000 ALTER TABLE `mod_user_authorizations` DISABLE KEYS */;
/*!40000 ALTER TABLE `mod_user_authorizations` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mod_users`
--

LOCK TABLES `mod_users` WRITE;
/*!40000 ALTER TABLE `mod_users` DISABLE KEYS */;
INSERT INTO `mod_users` VALUES (20,'张三','$2a$10$dqG1yMAPgUfuhHcmGFnw3e9EjXBaaFN6bxeIypzf9dLZTkpUBta2i',0,'13726139710',0,1);
/*!40000 ALTER TABLE `mod_users` ENABLE KEYS */;
UNLOCK TABLES;

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

LOCK TABLES `operator_attributes` WRITE;
/*!40000 ALTER TABLE `operator_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `operator_attributes` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='组织表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `organization`
--

LOCK TABLES `organization` WRITE;
/*!40000 ALTER TABLE `organization` DISABLE KEYS */;
/*!40000 ALTER TABLE `organization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rigid_body_attributes`
--

DROP TABLE IF EXISTS `rigid_body_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rigid_body_attributes` (
  `rigid_body_attributes_id` int NOT NULL AUTO_INCREMENT,
  `rigid_body_shape` int DEFAULT NULL,
  `center_of_mass` float DEFAULT NULL,
  `connection_information` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `material_attributes_id` int NOT NULL,
  `move` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`rigid_body_attributes_id`) USING BTREE,
  KEY `material_attributes_id` (`material_attributes_id`) USING BTREE,
  KEY `center_of_mass` (`center_of_mass`) USING BTREE,
  CONSTRAINT `rigid_body_attributes_ibfk_1` FOREIGN KEY (`material_attributes_id`) REFERENCES `material_attributes` (`material_attributes_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rigid_body_attributes`
--

LOCK TABLES `rigid_body_attributes` WRITE;
/*!40000 ALTER TABLE `rigid_body_attributes` DISABLE KEYS */;
/*!40000 ALTER TABLE `rigid_body_attributes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `server`
--

DROP TABLE IF EXISTS `server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `server` (
  `id` bigint NOT NULL COMMENT '服务器唯一标识ID',
  `name` varchar(127) DEFAULT NULL COMMENT '服务器名称',
  `ip` varchar(40) NOT NULL COMMENT '服务器IP地址',
  `type` varchar(16) DEFAULT NULL COMMENT '服务器类型',
  `status` varchar(16) NOT NULL COMMENT '服务器状态',
  `cpu_cores` int DEFAULT NULL COMMENT '服务器CPU核心数',
  `memory` int DEFAULT NULL COMMENT '服务器内存容量(GB)',
  `last_online` datetime DEFAULT NULL COMMENT '服务器最近一次上线时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务器集群表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `server`
--

LOCK TABLES `server` WRITE;
/*!40000 ALTER TABLE `server` DISABLE KEYS */;
/*!40000 ALTER TABLE `server` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `simulation_data`
--

DROP TABLE IF EXISTS `simulation_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `simulation_data` (
  `simulation_data_id` int NOT NULL,
  `data_file_name` varchar(255) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL COMMENT '''the type of data file(e.g.stl)''',
  `input_module` varchar(50) DEFAULT NULL COMMENT '''the input module of data file(such as 前处理)''',
  `output_module` varchar(50) DEFAULT NULL COMMENT '''the output module of data file(such as 前处理)''',
  `text_content` longtext COMMENT '''For text type,save this''',
  `binary_content` blob COMMENT '''For binary type,save this''',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '''update time for the first time''',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '''update time after the first time''',
  `description` mediumtext,
  PRIMARY KEY (`simulation_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `simulation_data`
--

LOCK TABLES `simulation_data` WRITE;
/*!40000 ALTER TABLE `simulation_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `simulation_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_organization`
--

DROP TABLE IF EXISTS `user_organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_organization` (
  `user_id` int NOT NULL COMMENT '用户ID',
  `org_id` int NOT NULL COMMENT '组织ID',
  PRIMARY KEY (`user_id`,`org_id`) USING BTREE,
  KEY `org_id` (`org_id`) USING BTREE,
  CONSTRAINT `user_organization_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `mod_users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_organization_ibfk_2` FOREIGN KEY (`org_id`) REFERENCES `organization` (`org_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户组织关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_organization`
--

LOCK TABLES `user_organization` WRITE;
/*!40000 ALTER TABLE `user_organization` DISABLE KEYS */;
INSERT INTO `user_organization` VALUES (1,1),(2,1),(5,1),(19,1);
/*!40000 ALTER TABLE `user_organization` ENABLE KEYS */;
UNLOCK TABLES;

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

-- Dump completed on 2026-02-05  1:18:25

-- 任务监控模块升级脚本
-- 用途: 为 mod_tasks 增加调度与监控字段

SET @db_name = DATABASE();

-- 列: server_id
SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND COLUMN_NAME = 'server_id'
);
SET @sql = IF(
    @col_exists = 0,
    "ALTER TABLE mod_tasks ADD COLUMN server_id INT NULL COMMENT '任务执行所在服务器ID'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 列: server_name
SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND COLUMN_NAME = 'server_name'
);
SET @sql = IF(
    @col_exists = 0,
    "ALTER TABLE mod_tasks ADD COLUMN server_name VARCHAR(255) NULL COMMENT '任务执行所在服务器名称'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 列: priority
SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND COLUMN_NAME = 'priority'
);
SET @sql = IF(
    @col_exists = 0,
    "ALTER TABLE mod_tasks ADD COLUMN priority INT NOT NULL DEFAULT 2 COMMENT '高=1，中=2，低=3'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 列: cpu_core_need
SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND COLUMN_NAME = 'cpu_core_need'
);
SET @sql = IF(
    @col_exists = 0,
    "ALTER TABLE mod_tasks ADD COLUMN cpu_core_need INT NOT NULL DEFAULT 1 COMMENT 'CPU核心需求'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 列: memory_need
SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND COLUMN_NAME = 'memory_need'
);
SET @sql = IF(
    @col_exists = 0,
    "ALTER TABLE mod_tasks ADD COLUMN memory_need INT NOT NULL DEFAULT 4 COMMENT '内存需求(GB)'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 列: progress
SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND COLUMN_NAME = 'progress'
);
SET @sql = IF(
    @col_exists = 0,
    "ALTER TABLE mod_tasks ADD COLUMN progress INT NOT NULL DEFAULT 0 COMMENT '执行进度(%)'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 列: start_time
SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND COLUMN_NAME = 'start_time'
);
SET @sql = IF(
    @col_exists = 0,
    "ALTER TABLE mod_tasks ADD COLUMN start_time DATETIME NULL COMMENT '任务开始时间'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 列: end_time
SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND COLUMN_NAME = 'end_time'
);
SET @sql = IF(
    @col_exists = 0,
    "ALTER TABLE mod_tasks ADD COLUMN end_time DATETIME NULL COMMENT '任务结束时间'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 列: error_msg
SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND COLUMN_NAME = 'error_msg'
);
SET @sql = IF(
    @col_exists = 0,
    "ALTER TABLE mod_tasks ADD COLUMN error_msg VARCHAR(512) NULL COMMENT '失败原因'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 索引: idx_mod_tasks_status_priority_ctime
SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND INDEX_NAME = 'idx_mod_tasks_status_priority_ctime'
);
SET @sql = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_mod_tasks_status_priority_ctime ON mod_tasks(status, priority, creation_time)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 索引: idx_mod_tasks_type_status
SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND INDEX_NAME = 'idx_mod_tasks_type_status'
);
SET @sql = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_mod_tasks_type_status ON mod_tasks(type, status)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 索引: idx_mod_tasks_server_id
SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'mod_tasks'
      AND INDEX_NAME = 'idx_mod_tasks_server_id'
);
SET @sql = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_mod_tasks_server_id ON mod_tasks(server_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;



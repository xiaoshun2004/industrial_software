package com.scut.industrial_software;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.MonitorTasksPageRequestDTO;
import com.scut.industrial_software.model.dto.TaskRuntimeSnapshotDTO;
import com.scut.industrial_software.model.vo.MonitorTaskItemVO;
import com.scut.industrial_software.service.IMonitorService;
import com.scut.industrial_software.service.IMonitorTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:monitor_task_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.main.allow-bean-definition-overriding=true",
                "spring.task.scheduling.enabled=false",
                "monitor.maxParallel.default=1"
        }
)
class MonitorTaskModuleIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IMonitorTaskService monitorTaskService;

    @Autowired
    private IMonitorService monitorService;

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    private RLock rLock;

    @SpyBean
    private com.scut.industrial_software.service.impl.MonitorServiceImpl monitorServiceSpy;

    @BeforeEach
    void setUp() throws InterruptedException {
        recreateTables();
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
    }

    @Test
    void shouldKeepTaskPendingWhenParallelLimitReached() {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("INSERT INTO server(id, name, instance_id, ip, specification, status, cpu_cores, memory, cpu_usage, memory_usage) VALUES(1,'S1','i-001','127.0.0.1','ecs.c7.large','running',8,32,'10.0','20.0')");

        jdbcTemplate.update("INSERT INTO mod_tasks(task_id, task_name, creator, creation_time, project_id, simulation_stage, type, status, compute_resource, priority, cpu_core_need, memory_need, progress) VALUES(1,'running-task',1,?,1,'求解器','结构','running','CPU',1,1,4,40)", Timestamp.valueOf(now.minusMinutes(2)));
        jdbcTemplate.update("INSERT INTO mod_tasks(task_id, task_name, creator, creation_time, project_id, simulation_stage, type, status, compute_resource, priority, cpu_core_need, memory_need, progress) VALUES(2,'pending-task',1,?,1,'求解器','结构','pending','CPU',1,1,4,0)", Timestamp.valueOf(now.minusMinutes(1)));

        ApiResult<?> result = monitorService.startProgram("task_2");

        assertEquals(200L, result.getCode());
        assertTrue(result.getMessage().contains("等待队列"));
        String status = jdbcTemplate.queryForObject("SELECT status FROM mod_tasks WHERE task_id = 2", String.class);
        assertEquals("pending", status);
    }

    @Test
    void shouldOnlyAllowPendingTaskToUpdatePriority() {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("INSERT INTO mod_tasks(task_id, task_name, creator, creation_time, project_id, simulation_stage, type, status, compute_resource, priority, cpu_core_need, memory_need, progress) VALUES(10,'pending-task',1,?,1,'求解器','多体','pending','CPU',2,1,4,0)", Timestamp.valueOf(now.minusMinutes(2)));
        jdbcTemplate.update("INSERT INTO mod_tasks(task_id, task_name, creator, creation_time, project_id, simulation_stage, type, status, compute_resource, priority, cpu_core_need, memory_need, progress) VALUES(11,'running-task',1,?,1,'求解器','多体','running','CPU',2,1,4,35)", Timestamp.valueOf(now.minusMinutes(1)));

        ApiResult<?> pendingUpdate = monitorTaskService.updateTaskPriority("task_10", 1);
        ApiResult<?> runningUpdate = monitorTaskService.updateTaskPriority("task_11", 1);

        assertEquals(200L, pendingUpdate.getCode());
        assertEquals(-1L, runningUpdate.getCode());
        Integer priority = jdbcTemplate.queryForObject("SELECT priority FROM mod_tasks WHERE task_id = 10", Integer.class);
        assertEquals(1, priority);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldOverlayRuntimeSnapshotInMonitorPage() {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("INSERT INTO mod_tasks(task_id, task_name, creator, creation_time, project_id, simulation_stage, type, status, compute_resource, server_id, server_name, priority, cpu_core_need, memory_need, progress, start_time) VALUES(20,'runtime-task',1,?,1,'求解器','冲击','running','CPU',2,'db-server',2,2,8,12,?)",
                Timestamp.valueOf(now.minusMinutes(3)), Timestamp.valueOf(now.minusMinutes(2)));

        TaskRuntimeSnapshotDTO snapshot = new TaskRuntimeSnapshotDTO(
                "task_20",
                "running",
                66,
                9,
                "runtime-server",
                now.minusMinutes(1),
                null
        );
        doReturn(snapshot).when(monitorServiceSpy).getRuntimeSnapshot("task_20");

        MonitorTasksPageRequestDTO requestDTO = new MonitorTasksPageRequestDTO();
        requestDTO.setPageNum(1);
        requestDTO.setPageSize(10);
        ApiResult<?> result = monitorTaskService.getTasksPage(requestDTO);

        assertEquals(200L, result.getCode());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        List<MonitorTaskItemVO> records = (List<MonitorTaskItemVO>) data.get("records");
        assertNotNull(records);
        assertEquals(1, records.size());
        MonitorTaskItemVO item = records.get(0);
        assertEquals("task_20", item.getTaskId());
        assertEquals(66, item.getProgress());
        assertEquals(9, item.getServerId());
        assertEquals("runtime-server", item.getServerName());
    }

    @Test
    void shouldUseExponentialBackoffWhenProcessMapIsEmpty() {
        ReflectionTestUtils.setField(monitorServiceSpy, "nextIdleDispatchAtMillis", 0L);
        ReflectionTestUtils.setField(monitorServiceSpy, "idleDispatchBackoffSeconds", 1L);

        monitorServiceSpy.scheduledMonitor();

        Long firstNextDispatchAt = (Long) ReflectionTestUtils.getField(monitorServiceSpy, "nextIdleDispatchAtMillis");
        Long firstBackoffSeconds = (Long) ReflectionTestUtils.getField(monitorServiceSpy, "idleDispatchBackoffSeconds");
        assertNotNull(firstNextDispatchAt);
        assertTrue(firstNextDispatchAt > System.currentTimeMillis());
        assertEquals(2L, firstBackoffSeconds);

        ReflectionTestUtils.setField(monitorServiceSpy, "nextIdleDispatchAtMillis", 0L);
        monitorServiceSpy.scheduledMonitor();

        Long secondNextDispatchAt = (Long) ReflectionTestUtils.getField(monitorServiceSpy, "nextIdleDispatchAtMillis");
        Long secondBackoffSeconds = (Long) ReflectionTestUtils.getField(monitorServiceSpy, "idleDispatchBackoffSeconds");
        assertNotNull(secondNextDispatchAt);
        assertNotEquals(firstNextDispatchAt, secondNextDispatchAt);
        assertEquals(4L, secondBackoffSeconds);
    }

    private void recreateTables() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS mod_tasks");
        jdbcTemplate.execute("DROP TABLE IF EXISTS mod_users");
        jdbcTemplate.execute("DROP TABLE IF EXISTS server");

        jdbcTemplate.execute("""
                CREATE TABLE mod_users (
                    user_id INT PRIMARY KEY,
                    username VARCHAR(64) NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE server (
                    id INT PRIMARY KEY,
                    instance_id VARCHAR(255),
                    name VARCHAR(255),
                    ip VARCHAR(255),
                    specification VARCHAR(255),
                    status VARCHAR(64),
                    cpu_cores INT,
                    memory INT,
                    cpu_usage VARCHAR(64),
                    memory_usage VARCHAR(64)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE mod_tasks (
                    task_id INT PRIMARY KEY,
                    task_name VARCHAR(255) NOT NULL,
                    creator INT NOT NULL,
                    creation_time TIMESTAMP NOT NULL,
                    project_id INT NOT NULL,
                    simulation_stage VARCHAR(50),
                    type VARCHAR(50),
                    status VARCHAR(50),
                    compute_resource VARCHAR(50),
                    server_id INT,
                    server_name VARCHAR(255),
                    priority INT,
                    cpu_core_need INT,
                    memory_need INT,
                    progress INT,
                    start_time TIMESTAMP,
                    end_time TIMESTAMP,
                    error_msg VARCHAR(512)
                )
                """);

        jdbcTemplate.update("INSERT INTO mod_users(user_id, username) VALUES(1, 'tester')");
    }
}


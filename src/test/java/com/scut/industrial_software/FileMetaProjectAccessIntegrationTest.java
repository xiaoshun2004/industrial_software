package com.scut.industrial_software;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.common.exception.ApiException;
import com.scut.industrial_software.model.dto.FileMetaUpdateDTO;
import com.scut.industrial_software.model.dto.FileQueryDTO;
import com.scut.industrial_software.model.dto.PageRequestDTO;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.model.entity.FileMeta;
import com.scut.industrial_software.model.vo.FileMetaVO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.service.IFileMetaService;
import com.scut.industrial_software.service.IModProjectsService;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.service.impl.TokenBlacklistService;
import com.scut.industrial_software.utils.DistributedLockUtil;
import com.scut.industrial_software.utils.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:file_project_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.main.allow-bean-definition-overriding=true",
                "spring.task.scheduling.enabled=false",
                "files.upload.path=target/test-file-upload"
        }
)
class FileMetaProjectAccessIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IFileMetaService fileMetaService;

    @Autowired
    private IModProjectsService modProjectsService;

    @Autowired
    private IModUsersService modUsersService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private DistributedLockUtil distributedLockUtil;

    @MockBean
    private RedissonClient redissonClient;

    @BeforeEach
    void setUp() throws Exception {
        recreateTables();
        insertBaseData();
        Files.createDirectories(Path.of("target/test-file-upload"));
        UserHolder.removeUser();
    }

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void shouldAllowOrganizationMemberToManageSharedProjectFiles() {
        saveCurrentUser(2, "member");

        FileMetaVO uploaded = uploadTextFile(10, "result", "shared content");
        PageVO<FileMetaVO> page = queryFiles(10);

        assertEquals(1L, page.getTotal());
        assertEquals(Integer.valueOf(10), page.getRecords().get(0).getProjectId());

        FileMetaUpdateDTO updateDTO = new FileMetaUpdateDTO();
        updateDTO.setFileName("renamed.txt");
        updateDTO.setDescription("updated description");
        FileMetaVO updated = fileMetaService.updateFileMeta(uploaded.getId(), 10, updateDTO);

        assertEquals("renamed.txt", updated.getFileName());
        assertEquals("updated description", updated.getDescription());
        assertArrayEquals("shared content".getBytes(), fileMetaService.downloadFile(uploaded.getId(), 10));

        saveCurrentUser(3, "outsider");
        assertThrows(ApiException.class, () -> fileMetaService.downloadFile(uploaded.getId(), 10));

        saveCurrentUser(2, "member");
        assertTrue(fileMetaService.deleteFile(uploaded.getId(), 10));
        assertEquals(Integer.valueOf(0), jdbcTemplate.queryForObject("SELECT COUNT(*) FROM file_meta WHERE file_uuid = ?", Integer.class, uploaded.getId()));
    }

    @Test
    void shouldAllowOnlyCreatorToManagePrivateProjectFiles() {
        saveCurrentUser(1, "owner");

        FileMetaVO uploaded = uploadTextFile(30, "private", "private content");

        saveCurrentUser(2, "member");
        assertThrows(ApiException.class, () -> fileMetaService.downloadFile(uploaded.getId(), 30));
        assertThrows(ApiException.class, () -> fileMetaService.deleteFile(uploaded.getId(), 30));

        saveCurrentUser(1, "owner");
        assertArrayEquals("private content".getBytes(), fileMetaService.downloadFile(uploaded.getId(), 30));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldListOnlyCurrentOrganizationSharedProjectsAndOwnPrivateProjectsAfterTransfer() {
        saveCurrentUser(1, "owner");

        ApiResult<Object> changeResult = modUsersService.updateUserOrganizationRelation(1, 2, 0);
        assertEquals(200L, changeResult.getCode());

        PageRequestDTO requestDTO = new PageRequestDTO();
        requestDTO.setPageNum(1);
        requestDTO.setPageSize(10);

        ApiResult<?> result = modProjectsService.getAccessibleProjectsPage(requestDTO);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
        List<Integer> projectIds = records.stream()
                .map(this::getProjectIdFromRecord)
                .toList();

        assertFalse(projectIds.contains(10));
        assertTrue(projectIds.contains(20));
        assertTrue(projectIds.contains(30));
    }

    @Test
    void shouldRejectMissingProjectAndMismatchedProject() {
        saveCurrentUser(2, "member");

        assertThrows(ApiException.class, () -> fileMetaService.uploadFile(
                "simulationResult",
                "missing",
                null,
                new MockMultipartFile("file", "missing.txt", "text/plain", "x".getBytes()),
                null
        ));

        FileMetaVO uploaded = uploadTextFile(10, "project-a", "content");

        saveCurrentUser(3, "outsider");
        assertThrows(ApiException.class, () -> fileMetaService.downloadFile(uploaded.getId(), 20));
    }

    @Test
    void shouldExcludeLegacyFilesWithoutProjectIdFromProjectFileList() {
        saveCurrentUser(2, "member");

        PageVO<FileMetaVO> page = queryFiles(10);

        assertEquals(0L, page.getTotal());
    }

    private FileMetaVO uploadTextFile(Integer projectId, String fileName, String content) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                fileName + ".txt",
                "text/plain",
                content.getBytes()
        );
        return fileMetaService.uploadFile("simulationResult", fileName, projectId, file, null);
    }

    private PageVO<FileMetaVO> queryFiles(Integer projectId) {
        FileQueryDTO queryDTO = new FileQueryDTO();
        queryDTO.setDbType("simulationResult");
        queryDTO.setProjectId(projectId);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        return fileMetaService.getMyFiles(queryDTO);
    }

    private void recreateTables() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS file_meta");
        jdbcTemplate.execute("DROP TABLE IF EXISTS mod_projects");
        jdbcTemplate.execute("DROP TABLE IF EXISTS user_organization");
        jdbcTemplate.execute("DROP TABLE IF EXISTS organization");
        jdbcTemplate.execute("DROP TABLE IF EXISTS mod_users");

        jdbcTemplate.execute("""
                CREATE TABLE mod_users (
                    user_id INT PRIMARY KEY,
                    username VARCHAR(64),
                    password VARCHAR(255),
                    permission INT,
                    task_permission INT,
                    phone VARCHAR(32),
                    version INT
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE organization (
                    org_id INT PRIMARY KEY,
                    org_name VARCHAR(100) NOT NULL,
                    create_user_id INT,
                    create_time TIMESTAMP NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE user_organization (
                    user_id INT NOT NULL,
                    org_id INT NOT NULL,
                    PRIMARY KEY (user_id, org_id)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE mod_projects (
                    project_id INT PRIMARY KEY,
                    project_name VARCHAR(255),
                    creator INT NOT NULL,
                    creation_time TIMESTAMP NOT NULL,
                    organization_id INT,
                    project_status INT
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE file_meta (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    file_uuid VARCHAR(255) NOT NULL,
                    file_name VARCHAR(255) NOT NULL,
                    file_path VARCHAR(512) NOT NULL,
                    file_size BIGINT,
                    file_type VARCHAR(100),
                    description TEXT,
                    creator_id INT,
                    creator_name VARCHAR(255),
                    storage_location VARCHAR(50),
                    create_time TIMESTAMP,
                    update_time TIMESTAMP,
                    db_type VARCHAR(30),
                    project_id INT,
                    preview_image_id VARCHAR(64),
                    preview_image_path VARCHAR(512),
                    preview_image_type VARCHAR(128),
                    preview_image_size BIGINT
                )
                """);
    }

    private void insertBaseData() {
        insertUser(1, "owner", 0);
        insertUser(2, "member", 0);
        insertUser(3, "outsider", 0);

        insertOrganization(1, "org-1", 1);
        insertOrganization(2, "org-2", 3);

        insertUserOrganization(1, 1);
        insertUserOrganization(2, 1);
        insertUserOrganization(3, 2);

        insertProject(10, "shared-org-1", 1, 1, 0);
        insertProject(20, "shared-org-2", 3, 2, 0);
        insertProject(30, "private-owner", 1, null, 1);
        insertLegacyFile();
    }

    private void insertUser(int userId, String username, int taskPermission) {
        jdbcTemplate.update(
                "INSERT INTO mod_users(user_id, username, password, permission, task_permission, phone, version) VALUES(?, ?, 'pwd', 0, ?, '13800000000', 0)",
                userId,
                username,
                taskPermission
        );
    }

    private void insertOrganization(int orgId, String orgName, int createUserId) {
        jdbcTemplate.update(
                "INSERT INTO organization(org_id, org_name, create_user_id, create_time) VALUES(?, ?, ?, ?)",
                orgId,
                orgName,
                createUserId,
                Timestamp.valueOf(LocalDateTime.now())
        );
    }

    private void insertUserOrganization(int userId, int orgId) {
        jdbcTemplate.update("INSERT INTO user_organization(user_id, org_id) VALUES(?, ?)", userId, orgId);
    }

    private void insertProject(int projectId, String projectName, int creator, Integer organizationId, int projectStatus) {
        jdbcTemplate.update(
                "INSERT INTO mod_projects(project_id, project_name, creator, creation_time, organization_id, project_status) VALUES(?, ?, ?, ?, ?, ?)",
                projectId,
                projectName,
                creator,
                Timestamp.valueOf(LocalDateTime.now()),
                organizationId,
                projectStatus
        );
    }

    private void insertLegacyFile() {
        FileMeta fileMeta = new FileMeta();
        jdbcTemplate.update(
                """
                        INSERT INTO file_meta(file_uuid, file_name, file_path, file_size, file_type, creator_id, creator_name,
                                              storage_location, create_time, update_time, db_type, project_id)
                        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                "legacy-file",
                "legacy.txt",
                "target/test-file-upload/legacy.txt",
                6L,
                "text/plain",
                2,
                "member",
                "LOCAL_DISK",
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now()),
                "simulationResult",
                fileMeta.getProjectId()
        );
    }

    private void saveCurrentUser(int userId, String username) {
        UserHolder.saveUser(new UserDTO(userId, username));
    }

    private Integer getProjectIdFromRecord(Map<String, Object> record) {
        Object value = record.get("projectId");
        if (value == null) {
            value = record.get("PROJECTID");
        }
        return ((Number) value).intValue();
    }
}

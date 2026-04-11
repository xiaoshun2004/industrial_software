package com.scut.industrial_software;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.controller.Admin.PermissionController;
import com.scut.industrial_software.model.dto.AddMembersDTO;
import com.scut.industrial_software.model.dto.CreateOrganizationDTO;
import com.scut.industrial_software.model.dto.UpdateGroupAdminDTO;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.model.dto.UserOrganizationDTO;
import com.scut.industrial_software.model.vo.UserOrganizationVO;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.service.IOrganizationService;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:organization_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.main.allow-bean-definition-overriding=true",
                "spring.task.scheduling.enabled=false"
        }
)
class OrganizationServiceIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IOrganizationService organizationService;

    @Autowired
    private IModUsersService modUsersService;

    @Autowired
    private PermissionController permissionController;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private DistributedLockUtil distributedLockUtil;

    @MockBean
    private RedissonClient redissonClient;

    @BeforeEach
    void setUp() {
        recreateTables();
        insertBaseUsers();
        UserHolder.removeUser();
    }

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void shouldCreateOrganizationAndAssignCreatorAsGroupAdmin() {
        saveCurrentUser(1, "creator");

        CreateOrganizationDTO dto = new CreateOrganizationDTO();
        dto.setOrgName("test-org");

        ApiResult<Object> result = organizationService.createOrganization(dto);

        assertEquals(200L, result.getCode());
        Integer relationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_organization WHERE user_id = 1 AND org_id = 1",
                Integer.class
        );
        Integer taskPermission = jdbcTemplate.queryForObject(
                "SELECT task_permission FROM mod_users WHERE user_id = 1",
                Integer.class
        );
        assertEquals(Integer.valueOf(1), relationCount);
        assertEquals(Integer.valueOf(1), taskPermission);
    }

    @Test
    void shouldRejectCreateOrganizationWhenUserAlreadyBelongsToAnotherOrganization() {
        insertOrganization(1, "existing-org", 99);
        insertUserOrganization(1, 1);
        saveCurrentUser(1, "creator");

        CreateOrganizationDTO dto = new CreateOrganizationDTO();
        dto.setOrgName("new-org");

        ApiResult<Object> result = organizationService.createOrganization(dto);

        assertEquals(-1L, result.getCode());
        Integer organizationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM organization", Integer.class);
        assertEquals(Integer.valueOf(1), organizationCount);
    }

    @Test
    void shouldAllowGroupAdminToAddMemberAndKeepProjectOwnershipUnchanged() {
        insertOrganization(1, "org-1", 1);
        insertUserOrganization(1, 1);
        updateUserTaskPermission(1, 1);
        insertProject(100, "project-a", 3, null);
        saveCurrentUser(1, "creator");

        AddMembersDTO dto = new AddMembersDTO();
        dto.setUserIds(List.of("3"));

        ApiResult<Object> result = organizationService.addMembersToOrganization(1, dto);

        assertEquals(200L, result.getCode());
        Integer relationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_organization WHERE user_id = 3 AND org_id = 1",
                Integer.class
        );
        Integer projectOrgId = jdbcTemplate.queryForObject(
                "SELECT organization_id FROM mod_projects WHERE project_id = 100",
                Integer.class
        );
        Integer taskPermission = jdbcTemplate.queryForObject(
                "SELECT task_permission FROM mod_users WHERE user_id = 3",
                Integer.class
        );
        assertEquals(Integer.valueOf(1), relationCount);
        assertNull(projectOrgId);
        assertEquals(Integer.valueOf(0), taskPermission);
    }

    @Test
    void shouldRejectAddMemberForOrdinaryMember() {
        insertOrganization(1, "org-1", 1);
        insertUserOrganization(1, 1);
        updateUserTaskPermission(1, 1);
        insertUserOrganization(2, 1);
        saveCurrentUser(2, "member");

        AddMembersDTO dto = new AddMembersDTO();
        dto.setUserIds(List.of("3"));

        ApiResult<Object> result = organizationService.addMembersToOrganization(1, dto);

        assertEquals(403L, result.getCode());
        Integer relationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_organization WHERE user_id = 3",
                Integer.class
        );
        assertEquals(Integer.valueOf(0), relationCount);
    }

    @Test
    void shouldPreventRemovingLastGroupAdmin() {
        insertOrganization(1, "org-1", 1);
        insertUserOrganization(1, 1);
        updateUserTaskPermission(1, 1);
        saveCurrentUser(99, "sysadmin");

        ApiResult<Object> result = organizationService.removeMemberFromOrganization(1, 1);

        assertEquals(-1L, result.getCode());
        Integer relationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_organization WHERE user_id = 1 AND org_id = 1",
                Integer.class
        );
        assertEquals(Integer.valueOf(1), relationCount);
    }

    @Test
    void shouldUpdateGroupAdminStatusBySystemAdmin() {
        insertOrganization(1, "org-1", 1);
        insertUserOrganization(1, 1);
        updateUserTaskPermission(1, 1);
        insertUserOrganization(2, 1);
        saveCurrentUser(99, "sysadmin");

        UpdateGroupAdminDTO dto = new UpdateGroupAdminDTO();
        dto.setTaskPermission(1);

        ApiResult<Object> result = organizationService.updateMemberTaskPermission(1, 2, dto);

        assertEquals(200L, result.getCode());
        Integer taskPermission = jdbcTemplate.queryForObject(
                "SELECT task_permission FROM mod_users WHERE user_id = 2",
                Integer.class
        );
        assertEquals(Integer.valueOf(1), taskPermission);
    }

    @Test
    void shouldReturnGroupAdminFlagInCurrentUserOrganization() {
        insertOrganization(1, "org-1", 2);
        insertUserOrganization(2, 1);
        updateUserTaskPermission(2, 1);
        saveCurrentUser(2, "member");

        UserOrganizationVO userOrganizationVO = modUsersService.getCurrentUserOrganization();

        assertNotNull(userOrganizationVO);
        assertEquals(Integer.valueOf(1), userOrganizationVO.getOrgId());
        assertEquals(Integer.valueOf(1), userOrganizationVO.getTaskPermission());
    }

    @Test
    void shouldRequireSystemAdminForChangeUserOrganizationController() {
        insertOrganization(1, "org-1", 1);

        UserOrganizationDTO dto = new UserOrganizationDTO();
        dto.setUserId("3");
        dto.setOrgId("1");

        saveCurrentUser(1, "creator");
        ApiResult<Object> forbiddenResult = permissionController.changeUserOrganization(dto);
        assertEquals(403L, forbiddenResult.getCode());
        assertNull(queryUserOrganization(3));

        saveCurrentUser(99, "sysadmin");
        ApiResult<Object> successResult = permissionController.changeUserOrganization(dto);
        assertEquals(200L, successResult.getCode());
        Integer relationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_organization WHERE user_id = 3 AND org_id = 1",
                Integer.class
        );
        Integer taskPermission = jdbcTemplate.queryForObject(
                "SELECT task_permission FROM mod_users WHERE user_id = 3",
                Integer.class
        );
        assertEquals(Integer.valueOf(1), relationCount);
        assertEquals(Integer.valueOf(0), taskPermission);
    }

    private void recreateTables() {
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
                    org_id INT AUTO_INCREMENT PRIMARY KEY,
                    org_name VARCHAR(100) NOT NULL,
                    create_user_id INT,
                    create_time TIMESTAMP NOT NULL,
                    CONSTRAINT organization_ibfk_1 FOREIGN KEY (create_user_id) REFERENCES mod_users(user_id)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE user_organization (
                    user_id INT NOT NULL,
                    org_id INT NOT NULL,
                    PRIMARY KEY (user_id, org_id),
                    CONSTRAINT user_organization_ibfk_1 FOREIGN KEY (user_id) REFERENCES mod_users(user_id),
                    CONSTRAINT user_organization_ibfk_2 FOREIGN KEY (org_id) REFERENCES organization(org_id)
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
    }

    private void insertBaseUsers() {
        jdbcTemplate.update("INSERT INTO mod_users(user_id, username, password, permission, task_permission, phone, version) VALUES(1, 'creator', 'pwd', 0, 0, '13800000001', 0)");
        jdbcTemplate.update("INSERT INTO mod_users(user_id, username, password, permission, task_permission, phone, version) VALUES(2, 'member', 'pwd', 0, 0, '13800000002', 0)");
        jdbcTemplate.update("INSERT INTO mod_users(user_id, username, password, permission, task_permission, phone, version) VALUES(3, 'outsider', 'pwd', 0, 0, '13800000003', 0)");
        jdbcTemplate.update("INSERT INTO mod_users(user_id, username, password, permission, task_permission, phone, version) VALUES(99, 'sysadmin', 'pwd', 1, 0, '13800000099', 0)");
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
        jdbcTemplate.update(
                "INSERT INTO user_organization(user_id, org_id) VALUES(?, ?)",
                userId,
                orgId
        );
    }

    private void updateUserTaskPermission(int userId, int taskPermission) {
        jdbcTemplate.update(
                "UPDATE mod_users SET task_permission = ? WHERE user_id = ?",
                taskPermission,
                userId
        );
    }

    private void insertProject(int projectId, String projectName, int creator, Integer organizationId) {
        jdbcTemplate.update(
                "INSERT INTO mod_projects(project_id, project_name, creator, creation_time, organization_id, project_status) VALUES(?, ?, ?, ?, ?, ?)",
                projectId,
                projectName,
                creator,
                Timestamp.valueOf(LocalDateTime.now()),
                organizationId,
                0
        );
    }

    private Integer queryUserOrganization(int userId) {
        List<Integer> orgIds = jdbcTemplate.query(
                "SELECT org_id FROM user_organization WHERE user_id = ?",
                (rs, rowNum) -> rs.getInt("org_id"),
                userId
        );
        return orgIds.isEmpty() ? null : orgIds.get(0);
    }

    private void saveCurrentUser(int userId, String username) {
        UserHolder.saveUser(new UserDTO(userId, username));
    }
}

package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.FileMetaMapper;
import com.scut.industrial_software.mapper.ModTasksMapper;
import com.scut.industrial_software.mapper.ModUsersMapper;
import com.scut.industrial_software.mapper.UserOrganizationMapper;
import com.scut.industrial_software.model.dto.PageRequestDTO;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.model.entity.FileMeta;
import com.scut.industrial_software.model.entity.UserOrganization;
import com.scut.industrial_software.utils.UserHolder;
import com.scut.industrial_software.model.dto.ProjectCreateDTO;
import com.scut.industrial_software.model.entity.ModProjects;
import com.scut.industrial_software.mapper.ModProjectsMapper;
import com.scut.industrial_software.model.entity.ModTasks;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.service.IModProjectsService;
import com.scut.industrial_software.service.IPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jhx1301
 * @since 2025-05-28
 */
@Service
public class ModProjectsServiceImpl extends ServiceImpl<ModProjectsMapper, ModProjects> implements IModProjectsService {

    private static final String INVALID_SIMULATION_TYPE_MESSAGE = "仿真类型不正确，应为：结构动力学、冲击动力学、多体动力学";

    private static final Set<String> VALID_SIMULATION_TYPES = new HashSet<>(Arrays.asList(
            "结构动力学",
            "冲击动力学",
            "多体动力学"
    ));

    @Autowired
    private ModTasksMapper modTasksMapper;

    @Autowired
    private ModUsersMapper modUsersMapper;

    @Autowired
    private FileMetaMapper fileMetaMapper;
    
    @Autowired
    private UserOrganizationMapper userOrganizationMapper;

    @Autowired
    private IPermissionService permissionService;

    @Override
    public ApiResult<?> getSharedProjectsPage(PageRequestDTO requestDTO) {
        // 获取当前登录用户信息
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return ApiResult.failed("用户未登录");
        }
        
        // 查询当前用户所属组织
        LambdaQueryWrapper<UserOrganization> userOrgWrapper = new LambdaQueryWrapper<>();
        userOrgWrapper.eq(UserOrganization::getUserId, currentUser.getId().intValue());
        UserOrganization userOrganization = userOrganizationMapper.selectOne(userOrgWrapper);
        
        Integer userOrgId = null;
        if (userOrganization != null) {
            userOrgId = userOrganization.getOrgId();
        }
        
        Page<Map<String, Object>> page = new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize());
        IPage<Map<String, Object>> pageResult = baseMapper.selectSharedProjectsByPage(page, requestDTO.getKeyword(), userOrgId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("size", pageResult.getSize());
        result.put("current", pageResult.getCurrent());
        
        return ApiResult.success(result);
    }

    @Override
    public ApiResult<?> getPrivateProjectsPage(PageRequestDTO requestDTO, Integer userId) {
        Page<Map<String, Object>> page = new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize());
        IPage<Map<String, Object>> pageResult = baseMapper.selectPrivateProjectsByPage(page, requestDTO.getKeyword(), userId);

        return ApiResult.success(buildPageResult(pageResult));
    }

    @Override
    public ApiResult<?> getAccessibleProjectsPage(PageRequestDTO requestDTO) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return ApiResult.failed("用户未登录");
        }

        Integer userOrgId = getCurrentUserOrganizationId(currentUser.getId().intValue());
        Page<Map<String, Object>> page = new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize());
        IPage<Map<String, Object>> pageResult = baseMapper.selectAccessibleProjectsByPage(
                page,
                requestDTO.getKeyword(),
                userOrgId,
                currentUser.getId().intValue()
        );

        return ApiResult.success(buildPageResult(pageResult));
    }

    @Override
    public ApiResult<?> createSharedProject(ProjectCreateDTO createDTO) {
        if (!StringUtils.hasText(createDTO.getProjectName())) {
            return ApiResult.failed("项目名称不能为空");
        }

        String simulationType = validateSimulationType(createDTO.getSimulationType());
        if (simulationType == null) {
            return ApiResult.failed(INVALID_SIMULATION_TYPE_MESSAGE);
        }

        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return ApiResult.failed("用户未登录");
        }

        ModUsers user = modUsersMapper.selectById(currentUser.getId().intValue());
        if (user == null) {
            return ApiResult.failed("创建者用户不存在");
        }

        Integer organizationId = getCurrentUserOrganizationId(currentUser.getId().intValue());
        if (organizationId == null) {
            return ApiResult.failed("用户未加入组织，无法创建共享项目");
        }
        
        ModProjects project = new ModProjects();
        project.setProjectName(createDTO.getProjectName());
        project.setCreator(user.getUserId());
        project.setSimulationType(simulationType);
        project.setOrganizationId(organizationId);
        project.setCreationTime(LocalDateTime.now());
        project.setProjectStatus(0); // 0表示共享项目
        
        boolean result = this.save(project);
        if (result) {
            return ApiResult.success(null, "创建成功");
        } else {
            return ApiResult.failed("创建失败");
        }
    }

    @Override
    public ApiResult<?> createPrivateProject(ProjectCreateDTO createDTO) {
        if (!StringUtils.hasText(createDTO.getProjectName())) {
            return ApiResult.failed("项目名称不能为空");
        }

        String simulationType = validateSimulationType(createDTO.getSimulationType());
        if (simulationType == null) {
            return ApiResult.failed(INVALID_SIMULATION_TYPE_MESSAGE);
        }

        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return ApiResult.failed("用户未登录");
        }

        ModUsers user = modUsersMapper.selectById(currentUser.getId().intValue());
        if (user == null) {
            return ApiResult.failed("创建者用户不存在");
        }
        
        ModProjects project = new ModProjects();
        project.setProjectName(createDTO.getProjectName());
        project.setCreator(user.getUserId());
        project.setSimulationType(simulationType);
        project.setOrganizationId(null);
        project.setCreationTime(LocalDateTime.now());
        project.setProjectStatus(1); // 1表示私人项目
        
        boolean result = this.save(project);
        if (result) {
            return ApiResult.success(null, "创建成功");
        } else {
            return ApiResult.failed("创建失败");
        }
    }

    @Override
    public ApiResult<?> encryptProject(Integer projectId) {
        ApiResult<ModProjects> accessResult = validateProjectManageAccess(projectId);
        if (accessResult.getCode() != 200L) {
            return accessResult;
        }
        ModProjects project = accessResult.getData();
        
        project.setProjectStatus(1); // 1表示私人项目
        project.setOrganizationId(null);
        boolean result = this.updateById(project);
        if (result) {
            return ApiResult.success(null, "项目已加密");
        } else {
            return ApiResult.failed("加密失败");
        }
    }

    @Override
    public ApiResult<?> decryptProject(Integer projectId) {
        ApiResult<ModProjects> accessResult = validateProjectManageAccess(projectId);
        if (accessResult.getCode() != 200L) {
            return accessResult;
        }
        ModProjects project = accessResult.getData();

        UserDTO currentUser = UserHolder.getUser();

        Integer organizationId = getCurrentUserOrganizationId(currentUser.getId().intValue());
        if (organizationId == null) {
            return ApiResult.failed("用户未加入组织，无法设为共享项目");
        }
        
        project.setProjectStatus(0); // 0表示共享项目
        project.setOrganizationId(organizationId);
        boolean result = this.updateById(project);
        if (result) {
            return ApiResult.success(null, "已取消加密");
        } else {
            return ApiResult.failed("取消加密失败");
        }
    }

    @Override
    @Transactional
    public ApiResult<?> deleteProject(Integer projectId) {
        ApiResult<ModProjects> accessResult = validateProjectManageAccess(projectId);
        if (accessResult.getCode() != 200L) {
            return accessResult;
        }
        ModProjects project = accessResult.getData();

        LambdaQueryWrapper<FileMeta> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(FileMeta::getProjectId, projectId);
        if (fileMetaMapper.selectCount(fileWrapper) > 0) {
            return ApiResult.failed("项目下存在文件，请先删除文件");
        }
        
        // 先删除项目下的所有任务
        LambdaQueryWrapper<ModTasks> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(ModTasks::getProjectId, projectId);
        modTasksMapper.delete(taskWrapper);
        
        // 再删除项目
        boolean result = this.removeById(projectId);
        if (result) {
            return ApiResult.success(null, "删除成功");
        } else {
            return ApiResult.failed("删除失败");
        }
    }

    private Integer getCurrentUserOrganizationId(Integer userId) {
        LambdaQueryWrapper<UserOrganization> userOrgWrapper = new LambdaQueryWrapper<>();
        userOrgWrapper.eq(UserOrganization::getUserId, userId);
        UserOrganization userOrganization = userOrganizationMapper.selectOne(userOrgWrapper);
        return userOrganization == null ? null : userOrganization.getOrgId();
    }

    private String validateSimulationType(String simulationType) {
        if (!StringUtils.hasText(simulationType)) {
            return null;
        }
        return VALID_SIMULATION_TYPES.contains(simulationType) ? simulationType : null;
    }

    private ApiResult<ModProjects> validateProjectManageAccess(Integer projectId) {
        ModProjects project = this.getById(projectId);
        if (project == null) {
            return ApiResult.failed("项目不存在");
        }

        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return ApiResult.failed("用户未登录");
        }

        if (Integer.valueOf(1).equals(project.getProjectStatus())) {
            if (!currentUser.getId().equals(project.getCreator())) {
                return ApiResult.failed("权限不足");
            }
            return ApiResult.success(project);
        }

        if (Integer.valueOf(0).equals(project.getProjectStatus())) {
            Integer organizationId = project.getOrganizationId();
            if (organizationId == null || !permissionService.canManageOrganization(organizationId)) {
                return ApiResult.failed("权限不足");
            }
            return ApiResult.success(project);
        }

        return ApiResult.failed("项目状态异常");
    }

    private Map<String, Object> buildPageResult(IPage<Map<String, Object>> pageResult) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("size", pageResult.getSize());
        result.put("current", pageResult.getCurrent());
        return result;
    }
}

package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.ModTasksMapper;
import com.scut.industrial_software.mapper.ModUsersMapper;
import com.scut.industrial_software.mapper.OrganizationMapper;
import com.scut.industrial_software.mapper.UserOrganizationMapper;
import com.scut.industrial_software.model.dto.PageRequestDTO;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.model.entity.UserOrganization;
import com.scut.industrial_software.utils.UserHolder;
import com.scut.industrial_software.model.dto.ProjectCreateDTO;
import com.scut.industrial_software.model.entity.ModProjects;
import com.scut.industrial_software.mapper.ModProjectsMapper;
import com.scut.industrial_software.model.entity.ModTasks;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.model.entity.Organization;
import com.scut.industrial_software.service.IModProjectsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    private ModTasksMapper modTasksMapper;

    @Autowired
    private ModUsersMapper modUsersMapper;

    @Autowired
    private OrganizationMapper organizationMapper;
    
    @Autowired
    private UserOrganizationMapper userOrganizationMapper;

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
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("size", pageResult.getSize());
        result.put("current", pageResult.getCurrent());
        
        return ApiResult.success(result);
    }

    @Override
    public ApiResult<?> createSharedProject(ProjectCreateDTO createDTO) {
        if (!StringUtils.hasText(createDTO.getProjectName())) {
            return ApiResult.failed("项目名称不能为空");
        }
        
        // 根据creator名字查找用户ID
        LambdaQueryWrapper<ModUsers> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(ModUsers::getUsername, createDTO.getCreator());
        ModUsers user = modUsersMapper.selectOne(userWrapper);
        if (user == null) {
            return ApiResult.failed("创建者用户不存在");
        }
        
        // 根据organization名字查找组织ID
        Integer organizationId = null;
        if (createDTO.getOrganization() != null && !createDTO.getOrganization().equals("无")) {
            LambdaQueryWrapper<Organization> orgWrapper = new LambdaQueryWrapper<>();
            orgWrapper.eq(Organization::getOrgName, createDTO.getOrganization());
            Organization organization = organizationMapper.selectOne(orgWrapper);
            if (organization == null) {
                return ApiResult.failed("组织不存在");
            }
            organizationId = organization.getOrgId();
        }
        
        ModProjects project = new ModProjects();
        project.setProjectName(createDTO.getProjectName());
        project.setCreator(user.getUserId());
        project.setOrganizationId(organizationId); // 可以为null
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
        
        // 根据creator名字查找用户ID
        LambdaQueryWrapper<ModUsers> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(ModUsers::getUsername, createDTO.getCreator());
        ModUsers user = modUsersMapper.selectOne(userWrapper);
        if (user == null) {
            return ApiResult.failed("创建者用户不存在");
        }
        
        // 根据organization名字查找组织ID
        Integer organizationId = null;
        if (createDTO.getOrganization() != null && !createDTO.getOrganization().equals("无")) {
            LambdaQueryWrapper<Organization> orgWrapper = new LambdaQueryWrapper<>();
            orgWrapper.eq(Organization::getOrgName, createDTO.getOrganization());
            Organization organization = organizationMapper.selectOne(orgWrapper);
            if (organization == null) {
                return ApiResult.failed("组织不存在");
            }
            organizationId = organization.getOrgId();
        }
        
        ModProjects project = new ModProjects();
        project.setProjectName(createDTO.getProjectName());
        project.setCreator(user.getUserId());
        project.setOrganizationId(organizationId); // 可以为null
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
        ModProjects project = this.getById(projectId);
        if (project == null) {
            return ApiResult.failed("项目不存在");
        }
        
        project.setProjectStatus(1); // 1表示私人项目
        boolean result = this.updateById(project);
        if (result) {
            return ApiResult.success(null, "项目已加密");
        } else {
            return ApiResult.failed("加密失败");
        }
    }

    @Override
    public ApiResult<?> decryptProject(Integer projectId) {
        ModProjects project = this.getById(projectId);
        if (project == null) {
            return ApiResult.failed("项目不存在");
        }
        
        project.setProjectStatus(0); // 0表示共享项目
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
        ModProjects project = this.getById(projectId);
        if (project == null) {
            return ApiResult.failed("项目不存在");
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
}

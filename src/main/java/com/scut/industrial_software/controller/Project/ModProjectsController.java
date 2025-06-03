package com.scut.industrial_software.controller.Project;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.PageRequestDTO;
import com.scut.industrial_software.model.dto.ProjectCreateDTO;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.service.IModProjectsService;
import com.scut.industrial_software.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  项目管理控制器
 * </p>
 *
 * @author jhx1301
 * @since 2025-05-28
 */
@RestController
@RequestMapping("/modProjects")
public class ModProjectsController {
    
    @Autowired
    private IModProjectsService modProjectsService;
    
    /**
     * 获取共享项目分页列表
     */
    @PostMapping("/shared/page")
    public ApiResult<?> getSharedProjectsPage(@RequestBody PageRequestDTO requestDTO) {
        return modProjectsService.getSharedProjectsPage(requestDTO);
    }
    
    /**
     * 获取私人项目分页列表
     */
    @PostMapping("/private/page")
    public ApiResult<?> getPrivateProjectsPage(@RequestBody PageRequestDTO requestDTO) {
        // 从用户上下文获取当前用户ID
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return ApiResult.failed("用户未登录");
        }
        return modProjectsService.getPrivateProjectsPage(requestDTO, currentUser.getId().intValue());
    }
    
    /**
     * 创建新共享项目
     */
    @PostMapping("/shared/create")
    public ApiResult<?> createSharedProject(@RequestBody ProjectCreateDTO createDTO) {
        return modProjectsService.createSharedProject(createDTO);
    }
    
    /**
     * 创建新私人项目
     */
    @PostMapping("/private/create")
    public ApiResult<?> createPrivateProject(@RequestBody ProjectCreateDTO createDTO) {
        return modProjectsService.createPrivateProject(createDTO);
    }
    
    /**
     * 加密项目（设为私有）
     */
    @PostMapping("/shared/{projectId}/encrypt")
    public ApiResult<?> encryptProject(@PathVariable Integer projectId) {
        return modProjectsService.encryptProject(projectId);
    }
    
    /**
     * 解密项目（设为共享）
     */
    @PostMapping("/private/{projectId}/decrypt")
    public ApiResult<?> decryptProject(@PathVariable Integer projectId) {
        return modProjectsService.decryptProject(projectId);
    }
    
    /**
     * 删除项目
     */
    @DeleteMapping("/delete/{projectId}")
    public ApiResult<?> deleteProject(@PathVariable Integer projectId) {
        return modProjectsService.deleteProject(projectId);
    }
}

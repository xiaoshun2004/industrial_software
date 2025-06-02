package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.PageRequestDTO;
import com.scut.industrial_software.model.dto.ProjectCreateDTO;
import com.scut.industrial_software.model.entity.ModProjects;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jhx1301
 * @since 2025-05-28
 */
public interface IModProjectsService extends IService<ModProjects> {
    
    /**
     * 获取共享项目分页列表
     * 
     * @param requestDTO 分页请求参数
     * @return 分页列表
     */
    ApiResult<?> getSharedProjectsPage(PageRequestDTO requestDTO);
    
    /**
     * 获取私人项目分页列表
     * 
     * @param requestDTO 分页请求参数
     * @param userId 用户ID
     * @return 分页列表
     */
    ApiResult<?> getPrivateProjectsPage(PageRequestDTO requestDTO, Integer userId);
    
    /**
     * 创建新共享项目
     * 
     * @param createDTO 创建参数
     * @return 创建结果
     */
    ApiResult<?> createSharedProject(ProjectCreateDTO createDTO);
    
    /**
     * 创建新私人项目
     * 
     * @param createDTO 创建参数
     * @return 创建结果
     */
    ApiResult<?> createPrivateProject(ProjectCreateDTO createDTO);
    
    /**
     * 加密项目（设为私有）
     * 
     * @param projectId 项目ID
     * @return 操作结果
     */
    ApiResult<?> encryptProject(Integer projectId);
    
    /**
     * 解密项目（设为共享）
     * 
     * @param projectId 项目ID
     * @return 操作结果
     */
    ApiResult<?> decryptProject(Integer projectId);
    
    /**
     * 删除项目
     * 
     * @param projectId 项目ID
     * @return 操作结果
     */
    ApiResult<?> deleteProject(Integer projectId);
}

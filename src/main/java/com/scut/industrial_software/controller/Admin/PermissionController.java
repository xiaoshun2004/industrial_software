package com.scut.industrial_software.controller.Admin;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.UserPageQueryDTO;
import com.scut.industrial_software.model.dto.UserPermissionDTO;
import com.scut.industrial_software.model.dto.UserOrganizationDTO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.model.vo.UserInfoVO;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.service.IPermissionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 权限管理系统控制器
 * 实现接口文档中的权限管理功能
 */
@RestController
@Slf4j
@RequestMapping
public class PermissionController {

    @Autowired
    private IModUsersService modUsersService;
    
    @Autowired
    private IPermissionService permissionService;

    /**
     * 获取用户分页列表
     * @param queryDTO 分页查询参数
     * @return 用户分页数据
     */
    @PostMapping("/user/page")
    public ApiResult<PageVO<UserInfoVO>> getUserPage(@Valid @RequestBody UserPageQueryDTO queryDTO) {
        log.info("获取用户分页列表，页码: {}, 页大小: {}, 关键字: {}", 
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getKeyword());
        
        PageVO<UserInfoVO> pageResult = modUsersService.pageUsers(queryDTO);
        return ApiResult.success(pageResult);
    }

    /**
     * 修改用户权限
     * @param permissionDTO 用户权限修改参数
     * @return 操作结果
     */
    @PostMapping("/user/permission")
    public ApiResult<Object> changeUserPermission(@Valid @RequestBody UserPermissionDTO permissionDTO) {
        log.info("修改用户权限，用户ID: {}, 目标权限: {}", 
                permissionDTO.getUserId(), permissionDTO.getPermission());
        
        try {
            // 检查当前用户是否有管理员权限
            if (!permissionService.isCurrentUserAdmin()) {
                return ApiResult.forbidden("没有权限执行此操作");
            }
            
            Integer userId = Integer.valueOf(permissionDTO.getUserId());
            
            // 使用并发安全的权限修改方法
            return permissionService.changeUserPermissionSafely(userId, permissionDTO.getPermission());
            
        } catch (NumberFormatException e) {
            log.warn("用户ID格式错误: {}", permissionDTO.getUserId());
            return ApiResult.failed("用户ID格式错误");
        } catch (Exception e) {
            log.error("修改用户权限时发生异常", e);
            return ApiResult.failed("权限修改失败，请稍后重试");
        }
    }

    /**
     * 修改用户所属组织
     * @param organizationDTO 用户组织修改参数
     * @return 操作结果
     */
    @PostMapping("/user/organization")
    public ApiResult<Object> changeUserOrganization(@Valid @RequestBody UserOrganizationDTO organizationDTO) {
        log.info("修改用户组织，用户ID: {}, 目标组织ID: {}", 
                organizationDTO.getUserId(), organizationDTO.getOrgId());
        
        return modUsersService.changeUserOrganization(organizationDTO);
    }
} 
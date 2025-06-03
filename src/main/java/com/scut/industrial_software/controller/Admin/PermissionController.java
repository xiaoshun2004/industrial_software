package com.scut.industrial_software.controller.Admin;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.UserPageQueryDTO;
import com.scut.industrial_software.model.dto.UserPermissionDTO;
import com.scut.industrial_software.model.dto.UserOrganizationDTO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.model.vo.UserInfoVO;
import com.scut.industrial_software.service.IModUsersService;
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
        
        // 校验权限值只能是 0 或 1
        if (permissionDTO.getPermission() != 0 && permissionDTO.getPermission() != 1) {
            return ApiResult.failed("权限值只能是 0（普通用户）或 1（管理员）");
        }
        
        Integer userId = Integer.valueOf(permissionDTO.getUserId());
        ApiResult<Object> result = modUsersService.changePermissionByAdmin(userId, permissionDTO.getPermission());
        
        // 如果成功，修改返回消息以符合接口文档
        if (result.getCode() == 200) {
            return ApiResult.success("权限修改成功");
        }
        
        return result;
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
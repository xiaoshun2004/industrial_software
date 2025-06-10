package com.scut.industrial_software.controller.User;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.vo.UserOrganizationVO;
import com.scut.industrial_software.service.IModUsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户组织信息控制器
 * </p>
 *
 * @author zhou
 * @since 2025-01-27
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserOrganizationController {

    @Autowired
    private IModUsersService modUsersService;

    /**
     * 获取当前登录用户的组织信息
     * 用于项目管理部分回显当前用户的组织
     * 
     * @return 用户组织信息，如果用户未加入组织则返回特定标识
     */
    @GetMapping("/organization")
    public ApiResult<UserOrganizationVO> getUserOrganization() {
        log.info("获取当前登录用户的组织信息");
        
        try {
            UserOrganizationVO userOrganization = modUsersService.getCurrentUserOrganization();
            
            if (userOrganization.getOrgId() == -1) {
                return ApiResult.success(userOrganization, "用户未加入任何组织");
            } else {
                return ApiResult.success(userOrganization, "成功");
            }
            
        } catch (Exception e) {
            log.error("获取用户组织信息失败", e);
            return ApiResult.failed("获取用户组织信息失败");
        }
    }
} 
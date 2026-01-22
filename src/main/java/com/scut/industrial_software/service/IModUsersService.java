package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.*;
import com.scut.industrial_software.model.entity.ModUsers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.model.vo.UserInfoVO;
import com.scut.industrial_software.model.vo.UserOrganizationVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhou
 * @since 2025-03-29
 */
public interface IModUsersService extends IService<ModUsers> {

    /**
     * 返回所有用户信息（测试）
     *
     * @return
     */
    List<ModUsers> getAllUsers();


    /**
     * 用户注册
     * @param userRegisterDTO
     * @return
     */
    ModUsers executeRegister(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    ModUsers login(UserLoginDTO userLoginDTO);

    /**
     * 用户分页查询
     * @param queryDTO
     * @return
     */
    PageVO<UserInfoVO> pageUsers(UserPageQueryDTO queryDTO);

    /**
     * 查询当前用户信息 回显
     * @return
     */
    UserInfoVO getCurrentUserInfo();

    /**
     * 用户修改密码
     * @param userId
     * @param changePasswordDTO
     * @return
     */
    ApiResult<Object> changePassword(Integer userId, @Valid ChangePasswordDTO changePasswordDTO);


    /**
     * 管理员修改用户信息
     * @param userId
     * @param updateDTO
     * @return
     */
    ApiResult<Object> updateUserInfoByAdmin(Integer userId, @Valid AdminUpdateUserInfoDTO updateDTO);

    /**
     * 管理员重置用户密码为123456
     * @param userId
     * @return
     */
    ApiResult<Object> resetPasswordByAdmin(Integer userId);

    /**
     * 管理员删除用户
     * @param userId
     * @return
     */
    ApiResult<Object> deleteUserByAdmin(Integer userId);

    /**
     * 管理员根据用户ID修改权限（只能是 0 或 1）
     * @param userId 用户ID
     * @param permission 新的权限值（0 或 1）
     * @return 操作结果
     */
    ApiResult<Object> changePermissionByAdmin(Integer userId, Integer permission);

    /**
     * 管理员修改用户所属组织
     * 功能说明：
     * 1. 修改用户的组织关联关系
     * 2. 自动同步更新该用户创建的所有项目的所属组织，保证数据一致性
     * 
     * @param userOrganizationDTO 用户组织修改DTO
     * @return 操作结果，包含新组织名称和同步更新的项目数量
     */
    ApiResult<Object> changeUserOrganization(UserOrganizationDTO userOrganizationDTO);

    /**
     * 获取当前登录用户的组织信息
     * @return 用户组织信息，如果用户未加入组织则返回特定标识
     */
    UserOrganizationVO getCurrentUserOrganization();

}

package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.*;
import com.scut.industrial_software.model.entity.ModUsers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scut.industrial_software.model.vo.LoginResponseVO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.model.vo.UserInfoVO;
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
    ApiResult<Object> changePassword(Long userId, @Valid ChangePasswordDTO changePasswordDTO);


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


}

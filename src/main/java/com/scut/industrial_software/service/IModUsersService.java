package com.scut.industrial_software.service;

import com.scut.industrial_software.model.dto.UserLoginDTO;
import com.scut.industrial_software.model.dto.UserPageQueryDTO;
import com.scut.industrial_software.model.dto.UserRegisterDTO;
import com.scut.industrial_software.model.entity.ModUsers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.model.vo.UserInfoVO;

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

    ModUsers login(UserLoginDTO userLoginDTO);

    /**
     * 用户分页查询
     * @param queryDTO
     * @return
     */
    PageVO<UserInfoVO> pageUsers(UserPageQueryDTO queryDTO);
}

package com.scut.industrial_software.service;

import com.scut.industrial_software.entity.ModUsers;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * @return
     */
    List<ModUsers> getAllUsers();
}

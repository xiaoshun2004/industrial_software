package com.scut.industrial_software.service.impl;

import com.scut.industrial_software.entity.ModUsers;
import com.scut.industrial_software.mapper.ModUsersMapper;
import com.scut.industrial_software.service.IModUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhou
 * @since 2025-03-29
 */
@Service
public class ModUsersServiceImpl extends ServiceImpl<ModUsersMapper, ModUsers> implements IModUsersService {

    /**
     * 返回所有用户信息（测试）
     * @return
     */
    public List<ModUsers> getAllUsers() {
        return this.list();
    }
}

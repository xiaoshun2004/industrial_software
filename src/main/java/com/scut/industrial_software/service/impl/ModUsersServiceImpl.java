package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scut.industrial_software.common.exception.ApiAsserts;
import com.scut.industrial_software.model.dto.UserRegisterDTO;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.mapper.ModUsersMapper;
import com.scut.industrial_software.service.IModUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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
     *
     * @return
     */
    public List<ModUsers> getAllUsers() {
        QueryWrapper<ModUsers> queryWrapper = new QueryWrapper<>();
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 用户注册
     * @param dto
     * @return
     */
    public ModUsers executeRegister(UserRegisterDTO dto) {
        //1.检查用户名是否存在
        LambdaQueryWrapper<ModUsers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModUsers::getUsername,dto.getUsername());
        ModUsers user = baseMapper.selectOne(wrapper);
        if(!ObjectUtils.isEmpty(user)){
            ApiAsserts.fail("用户名已经存在！");//该方法返回null并且抛出异常
        }
        ModUsers addUsers = ModUsers.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .permission(dto.getPermission())
                .build();
        baseMapper.insert(addUsers);
        return addUsers;
    }


}

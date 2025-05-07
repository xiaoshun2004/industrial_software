package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scut.industrial_software.common.exception.ApiAsserts;
import com.scut.industrial_software.model.dto.UserLoginDTO;
import com.scut.industrial_software.model.dto.UserPageQueryDTO;
import com.scut.industrial_software.model.dto.UserRegisterDTO;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.mapper.ModUsersMapper;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.model.vo.UserInfoVO;
import com.scut.industrial_software.service.IModUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.utils.PasswordUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
        String encodedPassword = PasswordUtil.encodePassword(dto.getPassword());
        ModUsers addUsers = ModUsers.builder()
                .username(dto.getUsername())
                .password(encodedPassword)
                .permission(dto.getPermission())
                .build();
        baseMapper.insert(addUsers);
        return addUsers;
    }

    @Override
    public ModUsers login(UserLoginDTO userLoginDTO) {
        // 1. 根据用户名查询用户（假设用户名唯一）
        LambdaQueryWrapper<ModUsers> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModUsers::getUsername, userLoginDTO.getUsername());
        ModUsers user = this.getOne(queryWrapper);

        // 2. 用户不存在或密码不匹配返回 null
        if (user == null || !PasswordUtil.matches(userLoginDTO.getPassword(), user.getPassword())) {
            return null;
        }

        return user;

    }

    /**
     * 用户分页查询
     * @param queryDTO
     * @return
     */
    public PageVO<UserInfoVO> pageUsers(UserPageQueryDTO queryDTO) {
        // 1. 构建分页参数
        Page<ModUsers> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<ModUsers> queryWrapper = new LambdaQueryWrapper<>();
        // 如果关键词不为空，按用户名模糊查询
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.like(ModUsers::getUsername, queryDTO.getKeyword());
        }

        // 3. 执行分页查询
        Page<ModUsers> userPage = this.page(page, queryWrapper);

        // 4. 转换实体为VO (直接在循环中使用BeanUtils)
        List<UserInfoVO> userVOList = new ArrayList<>();
        for (ModUsers user : userPage.getRecords()) {
            UserInfoVO vo = new UserInfoVO();
            BeanUtils.copyProperties(user, vo);
            userVOList.add(vo);
        }

        // 5. 构建并返回分页结果
        return PageVO.build(
                userVOList,
                userPage.getTotal(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );
    }


}

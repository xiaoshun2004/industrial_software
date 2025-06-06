package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.common.exception.ApiAsserts;
import com.scut.industrial_software.model.constant.RedisConstants;
import com.scut.industrial_software.model.dto.*;
import com.scut.industrial_software.model.entity.ModProjects;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.model.entity.Organization;
import com.scut.industrial_software.model.entity.UserOrganization;
import com.scut.industrial_software.mapper.ModUsersMapper;
import com.scut.industrial_software.mapper.ModProjectsMapper;
import com.scut.industrial_software.mapper.OrganizationMapper;
import com.scut.industrial_software.mapper.UserOrganizationMapper;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.model.vo.UserInfoVO;
import com.scut.industrial_software.service.IModUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.utils.PasswordUtil;
import com.scut.industrial_software.utils.UserHolder;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhou
 * @since 2025-03-29
 */
@Service
@Slf4j
public class ModUsersServiceImpl extends ServiceImpl<ModUsersMapper, ModUsers> implements IModUsersService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ModProjectsMapper modProjectsMapper;
    
    @Autowired
    private OrganizationMapper organizationMapper;
    
    @Autowired
    private UserOrganizationMapper userOrganizationMapper;

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
        wrapper.eq(ModUsers::getUsername, dto.getUsername());
        ModUsers user = baseMapper.selectOne(wrapper);
        if(!ObjectUtils.isEmpty(user)){
            ApiAsserts.fail("用户名已经存在！");//该方法返回null并且抛出异常
        }

        String encodedPassword = PasswordUtil.encodePassword(dto.getPassword());

        ModUsers addUsers = ModUsers.builder()
                .username(dto.getUsername())
                .password(encodedPassword)
                .permission(dto.getPermission())
                .phone(dto.getPhone())  // 添加电话号码
                .taskPermission(1)  // 默认组织权限
                .build();

        baseMapper.insert(addUsers);
        return addUsers;
    }

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
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

        // 4. 转换实体为VO，并添加组织信息
        List<UserInfoVO> userVOList = new ArrayList<>();
        for (ModUsers user : userPage.getRecords()) {
            UserInfoVO vo = new UserInfoVO();
            // 手动设置字段以符合响应格式
            vo.setUserId(String.valueOf(user.getUserId()));
            vo.setUsername(user.getUsername());
            vo.setPhone(user.getPhone());
            vo.setPermission(user.getPermission());
            // 从数据库字段读取taskPermission
            vo.setTaskPermission(user.getTaskPermission());
            
            // 查询并设置用户组织信息
            UserOrganization userOrg = getUserOrganization(user.getUserId());
            if (userOrg != null) {
                Organization organization = organizationMapper.selectById(userOrg.getOrgId());
                if (organization != null) {
                    vo.setOrganization(organization.getOrgName());
                    vo.setOrgId(String.valueOf(organization.getOrgId()));
                } else {
                    vo.setOrganization("无");
                    vo.setOrgId("");
                }
            } else {
                vo.setOrganization("无");
                vo.setOrgId("");
            }
            
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

    /**
     * 查询当前用户信息 回显
     * @return
     */
    public UserInfoVO getCurrentUserInfo() {
        // 从ThreadLocal中获取当前用户ID
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }

        // 根据用户ID查询用户完整信息
        return getUserInfoById(currentUser.getId());
    }

    /**
     * 用户修改密码
     * @param userId
     * @param changePasswordDTO
     * @return
     */
    @Transactional
    public ApiResult<Object> changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        // 获取用户信息
        ModUsers user = baseMapper.selectById(userId);
        if (user == null) {
            return ApiResult.failed("用户不存在");
        }

        // 验证旧密码
        if (!PasswordUtil.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            return ApiResult.failed("旧密码错误");
        }

        // 校验新旧密码是否相同
        if (changePasswordDTO.getOldPassword().equals(changePasswordDTO.getNewPassword())) {
            return ApiResult.failed("新密码不能与旧密码相同");
        }

        // 更新新密码
        String encodedNewPassword = PasswordUtil.encodePassword(changePasswordDTO.getNewPassword());
        user.setPassword(encodedNewPassword);

        // 调用更新方法，MyBatis-Plus 会自动加上版本条件并在成功时自增版本号
        boolean updateSuccess = this.updateById(user);

        // 如果更新失败，说明数据版本已被其他线程修改
        if (!updateSuccess) {
            return ApiResult.failed("用户信息已被修改，请刷新后重试");
        }

        return ApiResult.success("密码修改成功");
    }

    /**
     * 管理员修改用户信息
     * @param userId
     * @param updateDTO
     * @return
     */
    public ApiResult<Object> updateUserInfoByAdmin(Integer userId, AdminUpdateUserInfoDTO updateDTO) {
        // 获取用户信息
        ModUsers user = baseMapper.selectById(userId);
        if (user == null) {
            return ApiResult.failed("用户不存在");
        }

        // 检查用户名是否已存在（如果提供了新用户名）
        if (updateDTO.getUsername() != null && !updateDTO.getUsername().isEmpty()) {
            LambdaQueryWrapper<ModUsers> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ModUsers::getUsername, updateDTO.getUsername())
                    .ne(ModUsers::getUserId, userId); // 排除当前用户

            if (baseMapper.selectCount(wrapper) > 0) {
                return ApiResult.failed("用户名已被使用");
            }

            user.setUsername(updateDTO.getUsername());
        }

        // 更新电话号码（如果提供了新电话号码）
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }

        // 更新用户信息
        baseMapper.updateById(user);

        return ApiResult.success("用户基本信息更新成功");
    }

    /**
     * 管理员重置用户密码为123456
     * @param userId
     * @return
     */
    public ApiResult<Object> resetPasswordByAdmin(Integer userId) {
        // 获取用户信息
        ModUsers user = baseMapper.selectById(userId);
        if (user == null) {
            return ApiResult.failed("用户不存在");
        }
        // 默认密码
        String defaultPassword = "123456";
        // 加密密码
        String encodedPassword = PasswordUtil.encodePassword(defaultPassword);
        // 更新用户密码
        user.setPassword(encodedPassword);
        baseMapper.updateById(user);
        return ApiResult.success("密码已重置为默认密码：123456");
    }

    /**
     * 管理员删除用户
     * @param userId
     * @return
     */
    public ApiResult<Object> deleteUserByAdmin(Integer userId) {
        if (UserHolder.getUser().getId().equals(userId.longValue())) {
            return ApiResult.failed("管理员不能删除自己的账户");
        }
        // 获取用户信息
        ModUsers user = baseMapper.selectById(userId);
        if (user == null) {
            return ApiResult.failed("用户不存在");
        }

        try {
            String redisTokenKey = RedisConstants.USER_TOKEN_KEY_PREFIX + user.getUsername() + ":token";
            String token = redisTemplate.opsForValue().get(redisTokenKey);

            if (token != null && !token.isEmpty()) {
                // 加入黑名单，过期时间与 token 有效期一致
                tokenBlacklistService.addToBlacklist(token);
                /*// 可选：删除该 token
                redisTemplate.delete(redisTokenKey);*/
                log.info("用户 [{}] 的 token 已加入黑名单", user.getUsername());
            }

            // 删除用户
            baseMapper.deleteById(userId);
            return ApiResult.success("用户删除成功");
        } catch (Exception e) {
            // 捕获异常并记录日志
            log.error("删除用户失败", e);
            return ApiResult.failed("删除用户失败");
        }
    }


    /**
     * 根据用户ID查询用户信息
     * @param userId 用户ID
     * @return 用户信息VO
     */
    private UserInfoVO getUserInfoById(Long userId) {
        // 查询用户信息，排除密码字段
        ModUsers user = baseMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        // 转换为VO对象
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(String.valueOf(user.getUserId()));
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setPermission(user.getPermission());
        userInfoVO.setPhone(user.getPhone());
        // 从数据库字段读取taskPermission
        userInfoVO.setTaskPermission(user.getTaskPermission());
        
        // 查询并设置用户组织信息
        UserOrganization userOrg = getUserOrganization(user.getUserId());
        if (userOrg != null) {
            Organization organization = organizationMapper.selectById(userOrg.getOrgId());
            if (organization != null) {
                userInfoVO.setOrganization(organization.getOrgName());
                userInfoVO.setOrgId(String.valueOf(organization.getOrgId()));
            } else {
                userInfoVO.setOrganization("无");
                userInfoVO.setOrgId("");
            }
        } else {
            userInfoVO.setOrganization("无");
            userInfoVO.setOrgId("");
        }

        return userInfoVO;
    }

    /**
     * 根据用户ID获取用户所属组织关联
     * @param userId 用户ID
     * @return 用户组织关联对象，如果用户未分配组织则返回null
     */
    private UserOrganization getUserOrganization(Integer userId) {
        // 查询用户组织关联
        LambdaQueryWrapper<UserOrganization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserOrganization::getUserId, userId);
        return userOrganizationMapper.selectOne(wrapper);
    }

    /**
     * 根据用户ID获取用户所属组织名称
     * @param userId 用户ID
     * @return 组织名称，如果用户未分配组织则返回"无"
     */
    private String getUserOrganizationName(Integer userId) {
        UserOrganization userOrg = getUserOrganization(userId);
        
        if (userOrg == null) {
            return "无";
        }
        
        // 查询组织信息
        Organization organization = organizationMapper.selectById(userOrg.getOrgId());
        return organization != null ? organization.getOrgName() : "无";
    }

    @Override
    public ApiResult<Object> changePermissionByAdmin(Integer userId, Integer permission) {
        // 1. 查询用户是否存在
        ModUsers user = baseMapper.selectById(userId);
        if (user == null) {
            return ApiResult.failed("用户不存在");
        }
        // 2. 更新权限（0 或 1）
        user.setPermission(permission);
        baseMapper.updateById(user);
        log.info("管理员将用户 {} 的权限修改为 {}", userId, permission);
        return ApiResult.success("权限修改成功");
    }

    @Override
    @Transactional
    public ApiResult<Object> changeUserOrganization(UserOrganizationDTO userOrganizationDTO) {
        // 1. 查询用户是否存在
        Integer userId = Integer.valueOf(userOrganizationDTO.getUserId());
        ModUsers user = baseMapper.selectById(userId);
        if (user == null) {
            return ApiResult.failed("用户不存在");
        }

        // 2. 删除用户原有的组织关联
        LambdaQueryWrapper<UserOrganization> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserOrganization::getUserId, userId);
        userOrganizationMapper.delete(deleteWrapper);

        String newOrganizationName = "无";
        Integer newOrgId = null;
        
        // 3. 如果提供了新的组织ID且不为空字符串，则添加新的关联
        if (userOrganizationDTO.getOrgId() != null && !userOrganizationDTO.getOrgId().trim().isEmpty()) {
            newOrgId = Integer.valueOf(userOrganizationDTO.getOrgId());
            
            // 验证组织是否存在
            Organization organization = organizationMapper.selectById(newOrgId);
            if (organization == null) {
                return ApiResult.failed("目标组织不存在");
            }
            
            // 添加新的用户组织关联
            UserOrganization newUserOrg = UserOrganization.builder()
                    .userId(userId)
                    .orgId(newOrgId)
                    .build();
            userOrganizationMapper.insert(newUserOrg);
            
            newOrganizationName = organization.getOrgName();
        }

        // 4. 同步更新该用户创建的所有项目的所属组织
        LambdaQueryWrapper<ModProjects> projectWrapper = new LambdaQueryWrapper<>();
        projectWrapper.eq(ModProjects::getCreator, userId);
        List<ModProjects> userProjects = modProjectsMapper.selectList(projectWrapper);
        
        if (!userProjects.isEmpty()) {
            // 批量更新用户创建的项目的组织ID
            for (ModProjects project : userProjects) {
                project.setOrganizationId(newOrgId); // 可能为null，表示移出组织
                modProjectsMapper.updateById(project);
            }
            
            log.info("已同步更新用户 {} 创建的 {} 个项目的所属组织为: {}", 
                    userId, userProjects.size(), newOrganizationName);
        }

        log.info("用户 {} 的组织已修改为: {}", userId, newOrganizationName);
        
        // 5. 返回成功结果，包含新的组织名称和同步更新的项目数量
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("newOrganization", newOrganizationName);
        resultData.put("updatedProjectsCount", userProjects.size());
        return ApiResult.success(resultData, "用户组织修改成功，已同步更新相关项目");
    }

}

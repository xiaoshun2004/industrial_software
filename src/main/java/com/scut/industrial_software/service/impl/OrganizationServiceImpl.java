package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.OrganizationMapper;
import com.scut.industrial_software.mapper.UserOrganizationMapper;
import com.scut.industrial_software.model.dto.AddMembersDTO;
import com.scut.industrial_software.model.dto.CreateOrganizationDTO;
import com.scut.industrial_software.model.dto.OrganizationPageQueryDTO;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.model.entity.Organization;
import com.scut.industrial_software.model.entity.UserOrganization;
import com.scut.industrial_software.model.vo.OrganizationVO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.service.IOrganizationService;
import com.scut.industrial_software.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织表 服务实现类
 * </p>
 *
 * @author zhou
 * @since 2025-01-27
 */
@Service
@Slf4j
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements IOrganizationService {

    @Autowired
    private UserOrganizationMapper userOrganizationMapper;

    @Autowired
    private IModUsersService modUsersService;

    @Override
    public PageVO<OrganizationVO> pageOrganizations(OrganizationPageQueryDTO queryDTO) {
        // 1. 构建分页参数
        Page<OrganizationVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 执行分页查询
        Page<OrganizationVO> organizationPage = baseMapper.selectOrganizationPage(page, queryDTO.getKeyword());

        // 3. 构建并返回分页结果
        return PageVO.build(
                organizationPage.getRecords(),
                organizationPage.getTotal(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );
    }

    @Override
    public ApiResult<Object> createOrganization(CreateOrganizationDTO createDTO) {
        // 1. 检查组织名称是否已存在
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getOrgName, createDTO.getOrgName());
        Organization existingOrg = this.getOne(wrapper);
        
        if (existingOrg != null) {
            return ApiResult.failed("组织名称已存在");
        }

        // 2. 获取当前登录用户ID
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return ApiResult.failed("用户未登录");
        }

        // 3. 创建组织
        Organization organization = Organization.builder()
                .orgName(createDTO.getOrgName())
                .createUserId(currentUser.getId().intValue())
                .createTime(LocalDateTime.now())
                .build();

        // 4. 保存到数据库
        boolean saveResult = this.save(organization);
        if (!saveResult) {
            return ApiResult.failed("组织创建失败");
        }

        log.info("用户 {} 创建了新组织：{}", currentUser.getId(), createDTO.getOrgName());
        return ApiResult.success("组织创建成功");
    }

    @Override
    @Transactional
    public ApiResult<Object> addMembersToOrganization(Integer orgId, AddMembersDTO addMembersDTO) {
        // 1. 检查组织是否存在
        Organization organization = this.getById(orgId);
        if (organization == null) {
            return ApiResult.failed("组织不存在");
        }

        // 2. 验证用户ID并转换为Integer
        List<Integer> userIdList = new ArrayList<>();
        for (String userIdStr : addMembersDTO.getUserIds()) {
            try {
                Integer userId = Integer.valueOf(userIdStr);
                
                // 检查用户是否存在
                ModUsers user = modUsersService.getById(userId);
                if (user == null) {
                    return ApiResult.failed("用户ID " + userIdStr + " 不存在");
                }
                
                // 检查用户是否已经在组织中
                LambdaQueryWrapper<UserOrganization> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(UserOrganization::getUserId, userId)
                       .eq(UserOrganization::getOrgId, orgId);
                UserOrganization existingRelation = userOrganizationMapper.selectOne(wrapper);
                
                if (existingRelation == null) {
                    userIdList.add(userId);
                }
            } catch (NumberFormatException e) {
                return ApiResult.failed("用户ID格式不正确：" + userIdStr);
            }
        }

        if (userIdList.isEmpty()) {
            return ApiResult.failed("所有用户都已经在该组织中");
        }

        // 3. 批量添加关联关系
        List<UserOrganization> relations = new ArrayList<>();
        for (Integer userId : userIdList) {
            relations.add(UserOrganization.builder()
                    .userId(userId)
                    .orgId(orgId)
                    .build());
        }

        // 4. 批量插入
        for (UserOrganization relation : relations) {
            userOrganizationMapper.insert(relation);
        }

        log.info("成功添加 {} 个成员到组织 {}", userIdList.size(), orgId);
        return ApiResult.success("成功添加 " + userIdList.size() + " 个成员到组织");
    }

    @Override
    @Transactional
    public ApiResult<Object> removeMemberFromOrganization(Integer orgId, Integer memberId) {
        // 1. 检查组织是否存在
        Organization organization = this.getById(orgId);
        if (organization == null) {
            return ApiResult.failed("组织不存在");
        }

        // 2. 检查用户是否存在
        ModUsers user = modUsersService.getById(memberId);
        if (user == null) {
            return ApiResult.failed("用户不存在");
        }

        // 3. 检查用户组织关联关系是否存在
        LambdaQueryWrapper<UserOrganization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserOrganization::getUserId, memberId)
               .eq(UserOrganization::getOrgId, orgId);
        UserOrganization relation = userOrganizationMapper.selectOne(wrapper);

        if (relation == null) {
            return ApiResult.failed("该用户不在此组织中");
        }

        // 4. 删除关联关系
        int result = userOrganizationMapper.delete(wrapper);
        if (result > 0) {
            log.info("成功从组织 {} 中移除成员 {}", orgId, memberId);
            return ApiResult.success("成功移除组织成员");
        } else {
            return ApiResult.failed("移除成员失败");
        }
    }
} 
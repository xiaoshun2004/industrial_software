package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.OrganizationMapper;
import com.scut.industrial_software.mapper.UserOrganizationMapper;
import com.scut.industrial_software.model.dto.AddMembersDTO;
import com.scut.industrial_software.model.dto.CreateOrganizationDTO;
import com.scut.industrial_software.model.dto.MemberPageQueryDTO;
import com.scut.industrial_software.model.dto.OrganizationPageQueryDTO;
import com.scut.industrial_software.model.dto.UpdateGroupAdminDTO;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.model.entity.Organization;
import com.scut.industrial_software.model.entity.UserOrganization;
import com.scut.industrial_software.model.vo.MemberVO;
import com.scut.industrial_software.model.vo.OrganizationVO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.service.IOrganizationService;
import com.scut.industrial_software.service.IPermissionService;
import com.scut.industrial_software.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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

    @Autowired
    private IPermissionService permissionService;

    /**
     * 分页查询组织列表
     * @param queryDTO 查询参数，包含分页信息和关键词
     * @return 分页结果
     */
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

    /**
     * 创建新组织
     * @param createDTO 创建组织的请求参数
     * @return 创建结果
     */
    @Override
    @Transactional
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

        if (modUsersService.getUserOrganizationRelation(currentUser.getId()) != null) {
            return ApiResult.failed("用户已加入其他组织，无法重复创建组织");
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

        ApiResult<Object> relationResult = modUsersService.updateUserOrganizationRelation(
                currentUser.getId(),
                organization.getOrgId(),
                1
        );
        if (relationResult.getCode() != 200L) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return relationResult;
        }

        log.info("用户 {} 创建了新组织：{}", currentUser.getId(), createDTO.getOrgName());
        return ApiResult.success("组织创建成功");
    }

    /**
     * 向组织添加成员
     * @param orgId 组织ID
     * @param addMembersDTO 添加成员的请求参数，包含用户ID列表
     * @return 添加结果
     */
    @Override
    @Transactional
    public ApiResult<Object> addMembersToOrganization(Integer orgId, AddMembersDTO addMembersDTO) {
        // 1. 检查组织是否存在
        Organization organization = this.getById(orgId);
        if (organization == null) {
            return ApiResult.failed("组织不存在");
        }

        ApiResult<Object> permissionResult = validateOrganizationPermission(orgId);
        if (permissionResult != null) {
            return permissionResult;
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

                UserOrganization existingRelation = modUsersService.getUserOrganizationRelation(userId);
                if (existingRelation == null) {
                    userIdList.add(userId);
                } else if (!orgId.equals(existingRelation.getOrgId())) {
                    return ApiResult.failed("用户ID " + userIdStr + " 已加入其他组织");
                }
            } catch (NumberFormatException e) {
                return ApiResult.failed("用户ID格式不正确：" + userIdStr);
            }
        }

        if (userIdList.isEmpty()) {
            return ApiResult.failed("所有用户都已经在该组织中");
        }

        for (Integer userId : userIdList) {
            ApiResult<Object> result = modUsersService.updateUserOrganizationRelation(userId, orgId, 0);
            if (result.getCode() != 200L) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return result;
            }
        }

        log.info("成功添加 {} 个成员到组织 {}", userIdList.size(), orgId);
        return ApiResult.success("成功添加 " + userIdList.size() + " 个成员到组织");
    }

    /**
     * 从组织中移除成员
     * @param orgId 组织ID
     * @param memberId 成员用户ID
     * @return 移除结果
     */
    @Override
    @Transactional
    public ApiResult<Object> removeMemberFromOrganization(Integer orgId, Integer memberId) {
        // 1. 检查组织是否存在
        Organization organization = this.getById(orgId);
        if (organization == null) {
            return ApiResult.failed("组织不存在");
        }

        ApiResult<Object> permissionResult = validateOrganizationPermission(orgId);
        if (permissionResult != null) {
            return permissionResult;
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

        if (Integer.valueOf(1).equals(user.getTaskPermission()) && countGroupAdmins(orgId) <= 1) {
            return ApiResult.failed("组织至少保留一名组管理员");
        }

        ApiResult<Object> result = modUsersService.updateUserOrganizationRelation(memberId, null, 0);
        if (result.getCode() == 200L) {
            log.info("成功从组织 {} 中移除成员 {}", orgId, memberId);
            return ApiResult.success("成功移除组织成员");
        }

        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return result;
    }

    /**
     * 修改组成员管理员状态
     * @param orgId 组织ID
     * @param memberId 成员ID
     * @param updateDTO 管理员状态
     * @return 操作结果
     */
    @Override
    @Transactional
    public ApiResult<Object> updateMemberTaskPermission(Integer orgId, Integer memberId, UpdateGroupAdminDTO updateDTO) {
        Organization organization = this.getById(orgId);
        if (organization == null) {
            return ApiResult.failed("组织不存在");
        }

        ApiResult<Object> permissionResult = validateOrganizationPermission(orgId);
        if (permissionResult != null) {
            return permissionResult;
        }

        ModUsers user = modUsersService.getById(memberId);
        if (user == null) {
            return ApiResult.failed("用户不存在");
        }

        LambdaQueryWrapper<UserOrganization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserOrganization::getUserId, memberId)
                .eq(UserOrganization::getOrgId, orgId);
        UserOrganization relation = userOrganizationMapper.selectOne(wrapper);
        if (relation == null) {
            return ApiResult.failed("该用户不在此组织中");
        }

        int targetAdminFlag = updateDTO.getTaskPermission();
        if (Integer.valueOf(targetAdminFlag).equals(user.getTaskPermission())) {
            return ApiResult.success("组内权限未变化");
        }

        if (targetAdminFlag == 0 && Integer.valueOf(1).equals(user.getTaskPermission()) && countGroupAdmins(orgId) <= 1) {
            return ApiResult.failed("组织至少保留一名组管理员");
        }

        ApiResult<Object> result = modUsersService.updateUserOrganizationRelation(memberId, orgId, targetAdminFlag);
        if (result.getCode() == 200L) {
            return ApiResult.success("组内权限更新成功");
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return result;
    }

    /**
     * 分页查询组织成员列表
     * @param orgId 组织ID
     * @param queryDTO 查询参数，包含分页信息
     * @return 分页结果
     */
    @Override
    public PageVO<MemberVO> getOrganizationMembers(Integer orgId, MemberPageQueryDTO queryDTO) {
        // 1. 检查组织是否存在
        Organization organization = this.getById(orgId);
        if (organization == null) {
            // 如果组织不存在，返回空的分页结果
            return PageVO.build(
                    new ArrayList<>(),
                    0L,
                    queryDTO.getPageNum(),
                    queryDTO.getPageSize()
            );
        }

        // 2. 构建分页参数
        Page<MemberVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 3. 执行分页查询
        Page<MemberVO> memberPage = baseMapper.selectOrganizationMembers(page, orgId);

        // 4. 构建并返回分页结果
        return PageVO.build(
                memberPage.getRecords(),
                memberPage.getTotal(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );
    }

    /**
     * 分页查询未分配组织的成员列表
     * @param queryDTO 查询参数，包含分页信息
     * @return 分页结果
     */
    @Override
    public PageVO<MemberVO> getUnassignedMembers(MemberPageQueryDTO queryDTO) {
        // 1. 构建分页参数
        Page<MemberVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 执行分页查询
        Page<MemberVO> memberPage = baseMapper.selectUnassignedMembers(page);

        // 3. 构建并返回分页结果
        return PageVO.build(
                memberPage.getRecords(),
                memberPage.getTotal(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );
    }

    private ApiResult<Object> validateOrganizationPermission(Integer orgId) {
        if (!permissionService.canManageOrganization(orgId)) {
            return ApiResult.forbidden("没有权限执行此操作");
        }
        return null;
    }

    private int countGroupAdmins(Integer orgId) {
        LambdaQueryWrapper<UserOrganization> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(UserOrganization::getOrgId, orgId);
        List<UserOrganization> relations = userOrganizationMapper.selectList(relationWrapper);
        if (relations.isEmpty()) {
            return 0;
        }

        List<Integer> userIds = new ArrayList<>();
        for (UserOrganization relation : relations) {
            userIds.add(relation.getUserId());
        }

        LambdaQueryWrapper<ModUsers> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.in(ModUsers::getUserId, userIds)
                .eq(ModUsers::getTaskPermission, 1);
        return Math.toIntExact(modUsersService.count(adminWrapper));
    }
}

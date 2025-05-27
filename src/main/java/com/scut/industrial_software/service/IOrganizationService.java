package com.scut.industrial_software.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.AddMembersDTO;
import com.scut.industrial_software.model.dto.CreateOrganizationDTO;
import com.scut.industrial_software.model.dto.OrganizationPageQueryDTO;
import com.scut.industrial_software.model.entity.Organization;
import com.scut.industrial_software.model.vo.OrganizationVO;
import com.scut.industrial_software.model.vo.PageVO;

/**
 * <p>
 * 组织表 服务类
 * </p>
 *
 * @author zhou
 * @since 2025-01-27
 */
public interface IOrganizationService extends IService<Organization> {

    /**
     * 分页查询组织列表
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageVO<OrganizationVO> pageOrganizations(OrganizationPageQueryDTO queryDTO);

    /**
     * 新增组织
     * @param createDTO 新增组织参数
     * @return 操作结果
     */
    ApiResult<Object> createOrganization(CreateOrganizationDTO createDTO);

    /**
     * 添加成员到组织
     * @param orgId 组织ID
     * @param addMembersDTO 要添加的成员信息
     * @return 操作结果
     */
    ApiResult<Object> addMembersToOrganization(Integer orgId, AddMembersDTO addMembersDTO);

    /**
     * 移除组织成员
     * @param orgId 组织ID
     * @param memberId 成员ID
     * @return 操作结果
     */
    ApiResult<Object> removeMemberFromOrganization(Integer orgId, Integer memberId);
} 
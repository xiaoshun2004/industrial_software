package com.scut.industrial_software.controller.Organization;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.AddMembersDTO;
import com.scut.industrial_software.model.dto.CreateOrganizationDTO;
import com.scut.industrial_software.model.dto.OrganizationPageQueryDTO;
import com.scut.industrial_software.model.vo.OrganizationVO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.service.IOrganizationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 组织管理 前端控制器
 * </p>
 *
 * @author zhou
 * @since 2025-01-27
 */
@Slf4j
@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    @Autowired
    private IOrganizationService organizationService;

    /**
     * 获取组织分页列表
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @GetMapping
    public ApiResult<PageVO<OrganizationVO>> getOrganizationList(OrganizationPageQueryDTO queryDTO) {
        log.info("获取组织分页列表，页码：{}，每页数量：{}，关键词：{}", 
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getKeyword());
        
        PageVO<OrganizationVO> pageResult = organizationService.pageOrganizations(queryDTO);
        
        return ApiResult.success(pageResult);
    }

    /**
     * 新增组织
     * @param createDTO 新增组织参数
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Object> createOrganization(@Valid @RequestBody CreateOrganizationDTO createDTO) {
        log.info("新增组织，组织名称：{}", createDTO.getOrgName());
        
        return organizationService.createOrganization(createDTO);
    }

    /**
     * 添加成员到组织
     * @param orgId 组织ID
     * @param addMembersDTO 要添加的成员信息
     * @return 操作结果
     */
    @PostMapping("/{orgId}/members")
    public ApiResult<Object> addMembersToOrganization(
            @PathVariable Integer orgId, 
            @Valid @RequestBody AddMembersDTO addMembersDTO) {
        log.info("添加成员到组织，组织ID：{}，用户ID列表：{}", orgId, addMembersDTO.getUserIds());
        
        return organizationService.addMembersToOrganization(orgId, addMembersDTO);
    }

    /**
     * 移除组织成员
     * @param orgId 组织ID
     * @param memberId 成员ID
     * @return 操作结果
     */
    @DeleteMapping("/{orgId}/members/{memberId}")
    public ApiResult<Object> removeMemberFromOrganization(
            @PathVariable Integer orgId, 
            @PathVariable Integer memberId) {
        log.info("移除组织成员，组织ID：{}，成员ID：{}", orgId, memberId);
        
        return organizationService.removeMemberFromOrganization(orgId, memberId);
    }
} 
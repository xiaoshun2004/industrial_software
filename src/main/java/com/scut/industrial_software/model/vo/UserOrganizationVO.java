package com.scut.industrial_software.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户组织信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationVO {
    
    /**
     * 组织ID，如果用户未加入组织则为-1
     */
    private Integer orgId;
    
    /**
     * 组织名称，如果用户未加入组织则为空字符串
     */
    private String orgName;
} 
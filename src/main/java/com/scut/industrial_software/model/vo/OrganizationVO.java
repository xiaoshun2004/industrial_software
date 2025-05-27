package com.scut.industrial_software.model.vo;

import lombok.Data;

/**
 * 组织信息VO
 */
@Data
public class OrganizationVO {
    
    /**
     * 组织ID
     */
    private String orgId;
    
    /**
     * 组织名称
     */
    private String orgName;
    
    /**
     * 创建人姓名
     */
    private String creator;
    
    /**
     * 创建时间（ISO格式）
     */
    private String createTime;
} 
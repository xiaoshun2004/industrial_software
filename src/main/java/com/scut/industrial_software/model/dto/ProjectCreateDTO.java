package com.scut.industrial_software.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 项目创建请求参数
 */
@Data
public class ProjectCreateDTO {
    
    /**
     * 项目名称
     */
    @JsonProperty("project_name")
    private String projectName;
    
    /**
     * 创建者
     */
    private String creator;
    
    /**
     * 所属组织
     */
    private String organization;
} 
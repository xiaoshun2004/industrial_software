package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 项目实体类
 * </p>
 *
 * @author zhou
 * @since 2025-03-29
 */
@Getter
@Setter
@TableName("mod_projects")
public class ModProjects implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "project_id", type = IdType.AUTO)
    private Integer projectId;

    private String projectName;

    private String projectStage;

    private Integer creator;

    private LocalDateTime creationTime;
    
    /**
     * 所属组织ID
     */
    private Integer organizationId;
    
    /**
     * 项目状态：0-共享项目，1-私人项目
     */
    @TableField("project_status")
    private Integer projectStatus;
}

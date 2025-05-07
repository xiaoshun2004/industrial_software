package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * VIEW
 * </p>
 *
 * @author zhou
 * @since 2025-03-29
 */
@Getter
@Setter
@TableName("project_details_view")
public class ProjectDetailsView implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer projectId;

    private String projectName;

    private String projectStage;

    private Integer creator;

    private LocalDateTime creationTime;


}

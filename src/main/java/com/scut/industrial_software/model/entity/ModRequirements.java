package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhou
 * @since 2025-03-29
 */
@Getter
@Setter
@TableName("mod_requirements")
public class ModRequirements implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "requirement_id", type = IdType.AUTO)
    private Integer requirementId;

    private String requirementType;

    private String requirementParameters;

    private Integer taskId;


}

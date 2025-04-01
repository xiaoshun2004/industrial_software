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
@TableName("internal_force_solution_attributes")
public class InternalForceSolutionAttributes implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "internal_force_solution_id", type = IdType.AUTO)
    private Integer internalForceSolutionId;

    private String internalForceSolutionName;

    private String op2FilePath;

    private String internalForceSolutionDescription;

    private String internalForceSolutionType;


}

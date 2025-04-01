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
@TableName("operator_attributes")
public class OperatorAttributes implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "operator_id", type = IdType.AUTO)
    private Integer operatorId;

    private String operatorName;

    private String algorithmRealize;

    private String calculateObject;

    private String operatorDescription;

    private String applicationType;

    private String materialType;

    private String algorithmType;


}

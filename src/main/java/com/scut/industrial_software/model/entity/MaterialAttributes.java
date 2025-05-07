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
@TableName("material_attributes")
public class MaterialAttributes implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "material_attributes_id", type = IdType.AUTO)
    private Integer materialAttributesId;

    private Float density;

    private Float youngsModulus;

    private Float poissonRatio;

    private Float yieldStrength;

    private Float tensileStrength;

    private Float fractureToughness;


}

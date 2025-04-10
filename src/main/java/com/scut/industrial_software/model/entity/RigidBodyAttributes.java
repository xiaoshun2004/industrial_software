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
@TableName("rigid_body_attributes")
public class RigidBodyAttributes implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rigid_body_attributes_id", type = IdType.AUTO)
    private Integer rigidBodyAttributesId;

    private Integer rigidBodyShape;

    private Float centerOfMass;

    private String connectionInformation;

    private Integer materialAttributesId;

    private String move;


}

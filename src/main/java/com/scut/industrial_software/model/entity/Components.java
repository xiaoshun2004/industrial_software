package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@TableName("components")
@Accessors(chain = true)
public class Components {

    /**
     * 组件ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 组件名称
     */
    private String name;

    /**
     * 组件版本
     */
    private String version;

    /**
     * 组件大小
     */
    private String size;

    /**
     * 组件描述
     */
    private String description;

    /**
     * 动力学方向
     */
    private String dynamicsDirection;

    /**
     * 模块类型
     */
    private String moduleType;

    /**
     * 资源类型
     */
    private String resourceType;
}

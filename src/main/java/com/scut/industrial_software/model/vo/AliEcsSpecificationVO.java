package com.scut.industrial_software.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AliEcsSpecificationVO {
    /**
     * 规格
     */
    private String specification;

    /**
     * CPU核数
     */
    private Integer cpuCore;

    /**
     * 内存大小
     */
    private Integer memory;
}

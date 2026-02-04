package com.scut.industrial_software.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户证书申请视图对象
 */
@Data
@Accessors(chain = true)
public class ApplyLicenseVO {

    /**
     * 证书申请编号
     */
    private String applyId;

    /**
     * 客户公司名称
     */
    private String customerName;

    /**
     * 阶段名称
     */
    private String categoryId;

    /**
     * 模块阶段名称
     */
    private String moduleId;

    /**
     * 申请生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime validFrom;

    /**
     * 申请失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime validTo;

    /**
     * 可使用次数
     */
    private Integer usageCount;

    /**
     * 申请状态
     */
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
}

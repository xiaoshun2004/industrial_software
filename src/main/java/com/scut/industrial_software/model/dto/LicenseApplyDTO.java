package com.scut.industrial_software.model.dto;

import lombok.Data;

/**
 * 证书申请数据传输对象。
 */
@Data
public class LicenseApplyDTO {

    /** 客户名称（公司或个人）。 */
    private String customerName;

    /** 设备的唯一 MAC 地址。 */
    private String macAddress;

    /** 模块功能标识（前处理、后处理、求解器等）。 */
    private String categoryId;

    /** 模块名称（冲击、多体、结构等）。 */
    private String moduleId;

    /** 证书生效时间。 */
    private String validFrom;

    /** 证书失效时间。 */
    private String validTo;

    /** 证书剩余/允许使用次数。 */
    private Integer usageCount;
}

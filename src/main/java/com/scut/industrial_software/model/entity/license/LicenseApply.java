package com.scut.industrial_software.model.entity.license;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 映射 license_apply 证书申请表。
 */
@Data
@TableName("license_apply")
public class LicenseApply {

    @TableId(value = "request_id", type = IdType.INPUT)
    private String requestId;

    private String macAddress;

    private String status;

    private String licensePath;

    private String moduleId;

    private String categoryId;

    private String licenseNo;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    private Integer usageCount;

    private LocalDateTime createdAt;

    private String userName;

    private String customerName;
}

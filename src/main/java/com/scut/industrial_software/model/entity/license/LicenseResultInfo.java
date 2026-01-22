package com.scut.industrial_software.model.entity.license;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("license_result_info")
public class LicenseResultInfo {

    @TableId(value = "license_id", type = IdType.AUTO)
    private Integer licenseId;              // 证书ID（但此ID只用于基本存储，不代表证书内容）

    private String toolType;                // 工具类型（非空！！！）

    private String licensePath;             // .lic文件的存储路径（非空！！！）

    private String licenseName;             // 证书名称

    private Integer userId;                 // 证书所属用户ID（非空！！！且是外键）

    private LocalDateTime createTime;       // 证书生成时间（非空！！！）

    private LocalDateTime expireTime;       // 证书过期时间（非空！！！）

    private Boolean status;                 // 证书状态（非空！！！，如：有效/无效）

}

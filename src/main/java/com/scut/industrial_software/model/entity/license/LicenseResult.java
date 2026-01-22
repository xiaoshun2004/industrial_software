package com.scut.industrial_software.model.entity.license;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LicenseResult {

    private String licensePath;             // .lic文件的存储路径（非空！！！）

    private String licenseName;             // 证书名称

    private LocalDateTime createTime;       // 证书生成时间（非空！！！）

    private LocalDateTime expireTime;       // 证书过期时间（非空！！！）

    private String rawSecretKey;            // 生成的原始公钥（传送给客户端，可选）

    private byte[] licenseData;             // 证书文件的原始二进制数据（传送给客户端）

    public LicenseResult(String licPath, String rawSecretKey) {
        this.licensePath = licPath;
        this.rawSecretKey = rawSecretKey;
    }
}
